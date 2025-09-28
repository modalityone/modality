package one.modality.booking.frontoffice.bookingform.multipages;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import one.modality.booking.client.workingbooking.FXPersonToBook;
import one.modality.booking.client.workingbooking.HasWorkingBookingProperties;
import one.modality.booking.frontoffice.bookingelements.BookingElements;
import one.modality.booking.frontoffice.bookingelements.NavigationBar;
import one.modality.booking.frontoffice.bookingelements.PriceBar;
import one.modality.booking.frontoffice.bookingform.BookingFormBase;
import one.modality.booking.frontoffice.bookingform.BookingFormSettings;

/**
 * @author Bruno Salmon
 */
public abstract class MultiPageBookingForm extends BookingFormBase {

    private static final double MAX_WIDTH = 800;
    private static MonoPane LAST_PAGE_EMBEDDED_LOGIN_CONTAINER;

    private final NavigationBar navigationBar;
    private final TransitionPane transitionPane = new TransitionPane();
    private final BooleanProperty personToBookRequiredProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            getActivityCallback().setPersonToBookRequired(get());
        }
    };
    private final BooleanProperty showDefaultSubmitButtonProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            getActivityCallback().showDefaultSubmitButton(get());
        }
    };
    private final BooleanProperty pageShowingOwnSubmitButtonProperty = new SimpleBooleanProperty();
    private final BooleanProperty pageValidProperty = new SimpleBooleanProperty();
    private final IntegerProperty pageBusyCountProperty = new SimpleIntegerProperty();
    private final ObjectProperty<Future<?>> pageBusyFutureProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            Future<?> future = get();
            if (future != null && !future.isComplete()) {
                UiScheduler.scheduleDeferred(() -> {
                    ToggleButton nextButton = navigationBar.getNextButton();
                    nextButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                        future
                            .inUiThread()
                            .onComplete(ar -> {
                                pageBusyCountProperty.set(pageBusyCountProperty.get() + 1); // To force recomputation of next button disable property
                                navigationBar.updateButtons(); // To reestablish the content display
                            }), nextButton);
                });
            }
        }
    };
    private final BooleanProperty previousPageApplicableProperty = new SimpleBooleanProperty();
    private final BooleanProperty pageCanGoBackProperty = new SimpleBooleanProperty();
    private final BooleanProperty pageEndReachedProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (get())
                getActivityCallback().onEndReached();
        }
    };
    private int displayedPageIndex = -1;
    protected BookingFormPage displayedPage;
    private Unregisterable bookingFormPageValidListener;

    public MultiPageBookingForm(HasWorkingBookingProperties activity, BookingFormSettings settings) {
        super(activity, settings);
        if (!settings.showNavigationBar()) {
            navigationBar = null;
        } else {
            navigationBar = new NavigationBar();
            navigationBar.getBackButton().setOnMouseClicked(e -> navigateToPreviousPage());
            navigationBar.getBackButton().disableProperty().bind(new BooleanBinding() {
                {
                    super.bind(previousPageApplicableProperty, pageCanGoBackProperty);
                }

                @Override
                protected boolean computeValue() {
                    return !previousPageApplicableProperty.get() || !pageCanGoBackProperty.get();
                }
            });
            navigationBar.getNextButton().setOnMouseClicked(e -> navigateToNextPage());
            navigationBar.getNextButton().disableProperty().bind(new BooleanBinding() {
                {
                    super.bind(pageValidProperty, personToBookRequiredProperty, showDefaultSubmitButtonProperty, pageShowingOwnSubmitButtonProperty, pageBusyFutureProperty, pageBusyCountProperty, FXPersonToBook.personToBookProperty());
                }

                @Override
                protected boolean computeValue() {
                    if (isPageBusy())
                        return false;
                    // We disable the "next" button in the following cases:
                    // When the displayed page is not valid
                    return !pageValidProperty.get()
                           // When it is required to specify the person to book, and it's still not set on the booking nor on the person to book button
                           || personToBookRequiredProperty.get() && getWorkingBooking().getDocument().getPerson() == null && FXPersonToBook.getPersonToBook() == null
                           // When the page shows a submitButton (either the default one or its own)
                           || showDefaultSubmitButtonProperty.get() || pageShowingOwnSubmitButtonProperty.get()
                        ;
                }
            });
        }
    }

    protected abstract BookingFormPage[] getPages();

    @Override
    public Node buildUi() {
        BorderPane borderPane = new BorderPane(transitionPane);
        if (navigationBar != null) {
            borderPane.setTop(navigationBar.getView());
        }
        if (settings.showNavigationBar())
            borderPane.setBottom(new PriceBar(workingBookingProperties).getView());
        borderPane.setMaxWidth(MAX_WIDTH); // Max width for desktops
        return BookingElements.styleBookingElementsContainer(borderPane, true);
    }

    @Override
    public void onWorkingBookingLoaded() {
        if (displayedPageIndex == -1) {
            navigateToNextPage();
        }
    }

    @Override
    public ObservableBooleanValue transitingProperty() {
        return transitionPane.transitingProperty();
    }

    private boolean isPageBusy() {
        Future<?> busyFuture = pageBusyFutureProperty.get();
        return busyFuture != null && !busyFuture.isComplete();
    }

    public void navigateToPreviousPage() {
        navigateToPage(false);
    }

    public void navigateToNextPage() {
        navigateToPage(true);
    }

    private void navigateToPage(boolean forward) {
        int applicablePageIndex = findApplicablePageIndex(forward);
        if (applicablePageIndex >= 0)
            navigateToPage(applicablePageIndex);
    }

    private int findApplicablePageIndex(boolean forward) {
        // Not during transitions because 1) Transition is buggy in this case 2) this prevents accidental multiple clicks
        if (transitionPane.isTransiting())
            return -1;
        BookingFormPage[] pages = getPages();
        if (forward && displayedPageIndex >= 0 && !pages[displayedPageIndex].isValid())
            return -1;
        int increment = forward ? 1 : -1;
        for (int index = displayedPageIndex + increment; index >= 0 && index < pages.length; index += increment) {
            BookingFormPage predicatePage = pages[index];
            if (predicatePage.isApplicableToBooking(getWorkingBooking()))
                return index;
        }
        return -1;
    }

    private void navigateToPage(int index) {
        // Not during transitions because 1) Transition is buggy in this case 2) this prevents accidental multiple clicks
        if (transitionPane.isTransiting())
            return;
        boolean isForward = index > displayedPageIndex;
        if (isForward && isPageBusy())
            return;
        BookingFormPage[] pages = getPages();
        displayedPage = pages[index];
        displayedPageIndex = index;
        pageShowingOwnSubmitButtonProperty.set(displayedPage.isShowingOwnSubmitButton());
        displayedPage.setWorkingBookingProperties(workingBookingProperties);
        pageValidProperty.bind(displayedPage.validProperty());
        pageBusyFutureProperty.bind(displayedPage.busyFutureProperty());
        pageCanGoBackProperty.bind(displayedPage.canGoBackProperty());
        pageEndReachedProperty.bind(displayedPage.endReachedProperty());
        if (bookingFormPageValidListener != null)
            bookingFormPageValidListener.unregister();
        bookingFormPageValidListener = FXProperties.runNowAndOnPropertyChange(valid -> {
            getActivityCallback().disableSubmitButton(!valid);
        }, displayedPage.validProperty());
        MonoPane embeddedLoginContainer = displayedPage.getEmbeddedLoginContainer();
        if (embeddedLoginContainer != null && embeddedLoginContainer != LAST_PAGE_EMBEDDED_LOGIN_CONTAINER) {
            if (LAST_PAGE_EMBEDDED_LOGIN_CONTAINER != null)
                LAST_PAGE_EMBEDDED_LOGIN_CONTAINER.setContent(null);
            Region embeddedLoginNode = activityCallback.getEmbeddedLoginNode();
            embeddedLoginContainer.setContent(embeddedLoginNode);
            embeddedLoginContainer.visibleProperty().bind(embeddedLoginNode.visibleProperty());
            embeddedLoginContainer.managedProperty().bind(embeddedLoginNode.managedProperty());
            LAST_PAGE_EMBEDDED_LOGIN_CONTAINER = embeddedLoginContainer;
        }
        updateNavigationBar();
        updatePersonToBookRequired();
        updateShowDefaultSubmitButton();
        transitionPane.setReverse(!isForward);
        transitionPane.transitToContent(displayedPage.getView(), displayedPage::onTransitionFinished);
    }

    private boolean isLastPage() {
        return displayedPageIndex == getPages().length - 1;
    }

    protected void updateNavigationBar() {
        if (navigationBar != null) {
            navigationBar.setTitleI18nKey(displayedPage.getTitleI18nKey());
            previousPageApplicableProperty.set(findApplicablePageIndex(false) != -1);
        }
    }

    protected void updatePersonToBookRequired() {
        setPersonToBookRequired(displayedPage.getEmbeddedLoginContainer() != null);
    }

    protected void setPersonToBookRequired(boolean required) {
        personToBookRequiredProperty.set(required);
    }

    protected void updateShowDefaultSubmitButton() {
        setShowDefaultSubmitButton(isLastPage() && !displayedPage.isShowingOwnSubmitButton());
    }

    protected void setShowDefaultSubmitButton(boolean show) {
        showDefaultSubmitButtonProperty.set(show);
    }

}
