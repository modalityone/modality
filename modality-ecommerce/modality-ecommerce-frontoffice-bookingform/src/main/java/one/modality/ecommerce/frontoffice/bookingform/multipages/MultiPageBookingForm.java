package one.modality.ecommerce.frontoffice.bookingform.multipages;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import one.modality.ecommerce.client.workingbooking.FXPersonToBook;
import one.modality.ecommerce.client.workingbooking.HasWorkingBookingProperties;
import one.modality.ecommerce.frontoffice.bookingelements.NavigationBar;
import one.modality.ecommerce.frontoffice.bookingelements.PriceBar;
import one.modality.ecommerce.frontoffice.bookingform.BookingFormBase;
import one.modality.ecommerce.frontoffice.bookingform.BookingFormSettings;

/**
 * @author Bruno Salmon
 */
public abstract class MultiPageBookingForm extends BookingFormBase {

    private static final double MAX_WIDTH = 800;
    private static MonoPane LAST_PAGE_EMBEDDED_LOGIN_CONTAINER;

    private final NavigationBar navigationBar;
    private final TransitionPane transitionPane = new TransitionPane();
    private final BooleanProperty validProperty = new SimpleBooleanProperty();
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
    private int displayedPageIndex = -1;
    protected BookingFormPage displayedPage;
    private Unregisterable bookingFormPageValidListener;

    public MultiPageBookingForm(HasWorkingBookingProperties activity, BookingFormSettings settings) {
        super(activity, settings);
        Layouts.setFixedWidth(transitionPane, MAX_WIDTH);
        if (!settings.showNavigationBar()) {
            navigationBar = null;
        } else {
            navigationBar = new NavigationBar();
            navigationBar.getBackButton().setOnMouseClicked(e -> navigateToPreviousPage());
            navigationBar.getNextButton().setOnMouseClicked(e -> navigateToNextPage());
            navigationBar.getNextButton().disableProperty().bind(new BooleanBinding() { {
                super.bind(validProperty, personToBookRequiredProperty, showDefaultSubmitButtonProperty, pageShowingOwnSubmitButtonProperty, FXPersonToBook.personToBookProperty()); }

                @Override
                protected boolean computeValue() {
                    // We disable the "next" button in the following cases:
                    // When the displayed page is not valid
                    return !validProperty.get()
                           // When it is required to specify the person to book, and it's still not set on the booking nor on the person to book button
                           || personToBookRequiredProperty.get() && getWorkingBooking().getDocument().getPerson() == null && FXPersonToBook.getPersonToBook() == null
                           // When the page shows a submitButton (either the default one or its own)
                           || showDefaultSubmitButtonProperty.get() || pageShowingOwnSubmitButtonProperty.get();
                }
            });
        }
    }

    protected abstract BookingFormPage[] getPages();

    @Override
    public Node buildUi() {
        BorderPane container = new BorderPane(transitionPane);
        container.getStyleClass().add("booking-elements");
        if (navigationBar != null) {
            container.setTop(navigationBar.getView());
        }
        if (settings.showNavigationBar())
            container.setBottom(new PriceBar(workingBookingProperties).getView());
        container.setMaxWidth(MAX_WIDTH);
        return container;
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
            BookingFormPage candidatPage = pages[index];
            if (candidatPage.isApplicableToBooking(getWorkingBooking()))
                return index;
        }
        return -1;
    }

    private void navigateToPage(int index) {
        // Not during transitions because 1) Transition is buggy in this case 2) this prevents accidental multiple clicks
        if (transitionPane.isTransiting())
            return;
        boolean isBackwards = index < displayedPageIndex;
        BookingFormPage[] pages = getPages();
        displayedPage = pages[index];
        displayedPageIndex = index;
        pageShowingOwnSubmitButtonProperty.set(displayedPage.isShowingOwnSubmitButton());
        displayedPage.setWorkingBookingProperties(workingBookingProperties);
        validProperty.bind(displayedPage.validProperty());
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
        transitionPane.setReverse(isBackwards);
        transitionPane.transitToContent(displayedPage.getView());
    }

    private boolean isLastPage() {
        return displayedPageIndex == getPages().length - 1;
    }

    protected void updateNavigationBar() {
        if (navigationBar != null) {
            navigationBar.setTitleI18nKey(displayedPage.getTitleI18nKey());
            navigationBar.getBackButton().setDisable(findApplicablePageIndex(false) == -1);
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
