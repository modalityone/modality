package one.modality.booking.frontoffice.bookingpage;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import one.modality.booking.client.workingbooking.FXPersonToBook;
import one.modality.booking.client.workingbooking.HasWorkingBookingProperties;
import one.modality.booking.frontoffice.bookingelements.BookingElements;
import one.modality.booking.frontoffice.bookingelements.PriceBar;
import one.modality.booking.frontoffice.bookingform.BookingFormActivityCallback;
import one.modality.booking.frontoffice.bookingform.BookingFormBase;
import one.modality.booking.frontoffice.bookingform.BookingFormSettings;
import one.modality.booking.frontoffice.bookingpage.navigation.StandardBookingFormNavigation;

/**
 * @author Bruno Salmon
 */
public abstract class MultiPageBookingForm extends BookingFormBase {

    private static final double MAX_WIDTH = 800;
    private static MonoPane LAST_PAGE_EMBEDDED_LOGIN_CONTAINER;

    private BookingFormHeader header;
    private BookingFormNavigation navigation;
    protected final TransitionPane transitionPane = new TransitionPane(); {
        transitionPane.setScrollToTop(true); // scrolling to top each time the user navigates to a new step
    }
    private final BooleanProperty personToBookRequiredProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            BookingFormActivityCallback callback = getActivityCallback();
            if (callback != null)
                callback.setPersonToBookRequired(get());
        }
    };
    private final BooleanProperty showDefaultSubmitButtonProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            BookingFormActivityCallback callback = getActivityCallback();
            if (callback != null)
                callback.showDefaultSubmitButton(get());
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
                    if (navigation != null) {
                        ToggleButton nextButton = navigation.getNextButton();
                        nextButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                            future
                                .inUiThread()
                                .onComplete(ar -> {
                                    pageBusyCountProperty.set(pageBusyCountProperty.get() + 1); // To force recomputation of next button disable property
                                    navigation.updateState(); // To reestablish the content display
                                }),
                            nextButton);
                    }
                });
            }
        }
    };
    private final BooleanProperty previousPageApplicableProperty = new SimpleBooleanProperty();
    private final BooleanProperty pageCanGoBackProperty = new SimpleBooleanProperty();
    private final BooleanProperty pageCanGoForwardProperty = new SimpleBooleanProperty();
    private final BooleanProperty pageEndReachedProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (get()) {
                BookingFormActivityCallback callback = getActivityCallback();
                if (callback != null)
                    callback.onEndReached();
            }
        }
    };
    private final BooleanProperty pageIsPriceBarRelevantToShowProperty = new SimpleBooleanProperty();
    private int displayedPageIndex = -1;
    protected BookingFormPage displayedPage;
    private Unregisterable bookingFormPageValidListener;

    public MultiPageBookingForm(HasWorkingBookingProperties activity, BookingFormSettings settings) {
        super(activity, settings);
        if (settings.showNavigationBar()) {
            setNavigation(new StandardBookingFormNavigation());
        }
        setNavigationClickable(settings.isNavigationClickable());
    }

    public void setHeader(BookingFormHeader header) {
        this.header = header;
        if (header != null) {
            header.setBookingForm(this);
            header.setNavigationClickable(settings.isNavigationClickable());
        }
    }

    protected BookingFormHeader getHeader() {
        return header;
    }

    public void setNavigation(BookingFormNavigation navigation) {
        this.navigation = navigation;
        if (navigation != null) {
            navigation.setBookingForm(this);
            bindNavigationButtons();
        }
    }

    private void bindNavigationButtons() {
        // Use setOnAction instead of setOnMouseClicked because ButtonNavigation uses
        // nextToggleButton.fire() which triggers onAction, not onMouseClicked
        navigation.getBackButton().setOnAction(e -> navigateToPreviousPage());
        navigation.getBackButton().disableProperty().bind(previousPageApplicableProperty.not().or(pageCanGoBackProperty.not()));
        navigation.getNextButton().setOnAction(e -> navigateToNextPage());
        navigation.getNextButton().disableProperty().bind(new BooleanBinding() {
            {
                super.bind(pageValidProperty, pageCanGoForwardProperty, pageEndReachedProperty,
                    personToBookRequiredProperty, showDefaultSubmitButtonProperty,
                    pageShowingOwnSubmitButtonProperty,
                    pageBusyFutureProperty, pageBusyCountProperty, FXPersonToBook.personToBookProperty());
            }

            @Override
            protected boolean computeValue() {
                if (isPageBusy())
                    return false;
                // We disable the "next" button in the following cases:
                return !pageValidProperty.get()  // When the displayed page is not valid
                       || !pageCanGoForwardProperty.get()
                       || pageEndReachedProperty.get()
                       // When it is required to specify the person to book, and it's still not set on
                       // the booking nor on the person to book button
                       || personToBookRequiredProperty.get() && getWorkingBooking().getDocument().getPerson() == null
                          && FXPersonToBook.getPersonToBook() == null
                       // When the page shows a submitButton (either the default one or its own)
                       || showDefaultSubmitButtonProperty.get() || pageShowingOwnSubmitButtonProperty.get();
            }
        });
    }

    public abstract BookingFormPage[] getPages();

    public int getDisplayedPageIndex() {
        return displayedPageIndex;
    }

    /**
     * Returns the transition pane used to display pages.
     * This can be used by subclasses to show dynamic content not in the standard pages array.
     */
    protected TransitionPane getTransitionPane() {
        return transitionPane;
    }

    public BookingFormPage getDisplayedPage() {
        return displayedPage;
    }

    @Override
    public Node buildUi() {
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(transitionPane);
        if (header != null) {
            borderPane.setTop(header.getView());
        }
        if (navigation != null) {
            if (header != null) {
                // If header is present, navigation might be at the bottom or handled
                // differently.
                // For now, let's put it at the bottom if it's not null.
                // But wait, StandardBookingFormNavigation was at the top.
                // If we have a header (StepProgress), we probably want navigation at the
                // bottom?
                // The HTML mockup shows navigation buttons at the bottom.
                // The existing code put navigationBar at the top.
                // Let's assume if header is set, we put header at top.
                // Where to put navigation?
                // If navigation is StandardBookingFormNavigation (which looks like a header),
                // it might conflict.
                // Let's put navigation at the bottom if header is present, or check types?
                // Actually, the mockup shows "Continue" button at the bottom.
                // The StandardBookingFormNavigation has Back/Next buttons.
                // Let's put navigation at the bottom of the center pane or below it.
                // Let's try putting it at the bottom of the BorderPane.
                // But PriceBar is also at the bottom.
                // We can use a VBox for the bottom.
            } else {
                borderPane.setTop(navigation.getView());
            }
        }

        VBox bottomBox = new VBox();
        if (navigation != null && header != null) {
            Node navigationView = navigation.getView();
            // Hide navigation bar when page shows its own submit button (to avoid duplicate buttons)
            navigationView.visibleProperty().bind(pageShowingOwnSubmitButtonProperty.not());
            navigationView.managedProperty().bind(pageShowingOwnSubmitButtonProperty.not());
            bottomBox.getChildren().add(navigationView);
        }

        if (settings.showPriceBar()) {
            Node priceBarView = new PriceBar(workingBookingProperties).getView();
            Layouts.bindManagedAndVisiblePropertiesTo(pageIsPriceBarRelevantToShowProperty, priceBarView);
            bottomBox.getChildren().add(priceBarView);
        }

        if (!bottomBox.getChildren().isEmpty()) {
            borderPane.setBottom(bottomBox);
        }

        borderPane.setMaxWidth(MAX_WIDTH); // Max width for desktops

        // Wrap in StackPane with TOP_CENTER alignment to position form at the top
        StackPane wrapper = new StackPane(borderPane);
        StackPane.setAlignment(borderPane, Pos.TOP_CENTER);

        return BookingElements.styleBookingElementsContainer(wrapper, true);
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
        // Not during transitions because 1) Transition is buggy in this case 2) this
        // prevents accidental multiple clicks
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

    public void navigateToPage(int index) {
        // Not during transitions because 1) Transition is buggy in this case 2) this
        // prevents accidental multiple clicks
        if (transitionPane.isTransiting())
            return;
        boolean isForward = index > displayedPageIndex;
        if (isForward && isPageBusy())
            return;
        BookingFormPage[] pages = getPages();
        displayedPage = pages[index];
        displayedPageIndex = index;
        pageShowingOwnSubmitButtonProperty.set(displayedPage.isShowingOwnSubmitButton());
        pageIsPriceBarRelevantToShowProperty.set(displayedPage.isPriceBarRelevantToShow());
        displayedPage.setWorkingBookingProperties(workingBookingProperties);
        pageValidProperty.bind(displayedPage.validProperty());
        pageBusyFutureProperty.bind(displayedPage.busyFutureProperty());
        pageCanGoBackProperty.bind(displayedPage.canGoBackProperty());
        pageCanGoForwardProperty.bind(displayedPage.canGoForwardProperty());
        pageEndReachedProperty.bind(displayedPage.endReachedProperty());
        if (bookingFormPageValidListener != null)
            bookingFormPageValidListener.unregister();
        // Only register callback listener if activityCallback is available (may be null in resume payment flow)
        if (activityCallback != null) {
            bookingFormPageValidListener = FXProperties.runNowAndOnPropertyChange(valid -> getActivityCallback().disableSubmitButton(!valid), displayedPage.validProperty());
        }
        MonoPane embeddedLoginContainer = displayedPage.getEmbeddedLoginContainer();
        if (embeddedLoginContainer != null && embeddedLoginContainer != LAST_PAGE_EMBEDDED_LOGIN_CONTAINER && activityCallback != null) {
            if (LAST_PAGE_EMBEDDED_LOGIN_CONTAINER != null)
                LAST_PAGE_EMBEDDED_LOGIN_CONTAINER.setContent(null);
            Region embeddedLoginNode = activityCallback.getEmbeddedLoginNode();
            embeddedLoginContainer.setContent(embeddedLoginNode);
            embeddedLoginContainer.visibleProperty().bind(embeddedLoginNode.visibleProperty());
            embeddedLoginContainer.managedProperty().bind(embeddedLoginNode.managedProperty());
            LAST_PAGE_EMBEDDED_LOGIN_CONTAINER = embeddedLoginContainer;
        }

        // Update Header Visibility
        if (header != null) {
            Layouts.setManagedAndVisibleProperties(header.getView(), displayedPage.isHeaderVisible());
        }

        // Update Navigation Buttons
        updateNavigationButtons();

        updateNavigationBar();
        updatePersonToBookRequired();
        updateShowDefaultSubmitButton();
        transitionPane.setReverse(!isForward);
        transitionPane.transitToContent(displayedPage.getView(), displayedPage::onTransitionFinished);
    }

    private void updateNavigationButtons() {
        if (navigation == null)
            return;

        BookingFormButton[] buttons = displayedPage.getButtons();
        if (buttons != null) {
            navigation.setButtons(buttons);
        } else {
            // Fallback to default behavior if no custom buttons defined
            // We might need to reset the navigation to default state if it was changed
            // But StandardBookingFormNavigation doesn't support "resetting" easily if we
            // overwrite it.
            // For now, we assume if getButtons() returns null, the navigation component
            // handles its default state
            // or we need to explicitly tell it to use defaults.
            // Let's pass null to setButtons to indicate "use defaults"
            navigation.setButtons((BookingFormButton[]) null);
        }
    }

    private boolean isLastPage() {
        return displayedPageIndex == getPages().length - 1;
    }

    protected void updateNavigationBar() {
        if (navigation != null) {
            navigation.updateState();
            previousPageApplicableProperty.set(findApplicablePageIndex(false) != -1);
        }
        if (header != null) {
            header.updateState();
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

    public void setNavigationClickable(boolean clickable) {
        if (header != null) {
            header.setNavigationClickable(clickable);
        }
    }

    /**
     * Navigates to a special page that is NOT part of the standard pages array.
     * This is used for dynamic pages like sold-out recovery that are created on-demand
     * and don't belong in the static page flow.
     *
     * <p>The page is displayed with its own navigation buttons (via getButtons())
     * and all standard page bindings are established.</p>
     *
     * @param page The special page to navigate to
     */
    protected void navigateToSpecialPage(BookingFormPage page) {
        // Not during transitions
        if (transitionPane.isTransiting())
            return;

        // Mark as special page (not in pages array)
        displayedPage = page;
        displayedPageIndex = -1;

        // Setup the page
        page.setWorkingBookingProperties(workingBookingProperties);

        // Bind page properties
        pageShowingOwnSubmitButtonProperty.set(page.isShowingOwnSubmitButton());
        pageIsPriceBarRelevantToShowProperty.set(page.isPriceBarRelevantToShow());
        pageValidProperty.bind(page.validProperty());
        pageBusyFutureProperty.bind(page.busyFutureProperty());
        pageCanGoBackProperty.bind(page.canGoBackProperty());
        pageCanGoForwardProperty.bind(page.canGoForwardProperty());
        pageEndReachedProperty.bind(page.endReachedProperty());

        // Unregister previous listener
        if (bookingFormPageValidListener != null)
            bookingFormPageValidListener.unregister();

        // Register new callback listener if available
        if (activityCallback != null) {
            bookingFormPageValidListener = FXProperties.runNowAndOnPropertyChange(
                valid -> getActivityCallback().disableSubmitButton(!valid),
                page.validProperty());
        }

        // Update Header Visibility
        if (header != null) {
            Layouts.setManagedAndVisibleProperties(header.getView(), page.isHeaderVisible());
        }

        // Update Navigation Buttons (use page's custom buttons)
        updateNavigationButtons();

        // Update navigation bar and don't allow back navigation from special pages
        previousPageApplicableProperty.set(false);
        updateShowDefaultSubmitButton();

        // Transition to the page
        transitionPane.setReverse(false);
        transitionPane.transitToContent(page.getView(), page::onTransitionFinished);
    }

}
