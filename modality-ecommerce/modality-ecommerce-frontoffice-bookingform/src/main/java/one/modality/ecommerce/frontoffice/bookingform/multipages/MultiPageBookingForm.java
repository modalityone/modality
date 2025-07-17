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
                    //!valid || personToBookRequired && personToBook == null || showDefaultSubmitButton || pageShowingOwnSubmitButton
                    return !validProperty.get() || personToBookRequiredProperty.get() && FXPersonToBook.getPersonToBook() == null || showDefaultSubmitButtonProperty.get() || pageShowingOwnSubmitButtonProperty.get();
                }
            });
        }
    }

    protected abstract BookingFormPage[] getPages();

    @Override
    public Node buildUi() {
        BorderPane container = new BorderPane(transitionPane);
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
            navigateToPage(0);
        }
    }

    @Override
    public ObservableBooleanValue transitingProperty() {
        return transitionPane.transitingProperty();
    }

    public void navigateToPreviousPage() {
        if (displayedPageIndex > 0)
            navigateToPage(displayedPageIndex - 1);
    }

    public void navigateToNextPage() {
        BookingFormPage[] pages = getPages();
        if (displayedPageIndex < 0)
            navigateToPage(0);
        else {
            BookingFormPage bookingFormPage = pages[displayedPageIndex];
            if (bookingFormPage.isValid() && displayedPageIndex < pages.length - 1)
                navigateToPage(displayedPageIndex + 1);
        }
    }

    private void navigateToPage(int index) {
        // Not during transitions because 1) Transition is buggy in this case 2) this prevents accidental multiple clicks
        if (transitionPane.isTransiting())
            return;
        BookingFormPage[] pages = getPages();
        displayedPage = pages[index];
        pageShowingOwnSubmitButtonProperty.set(displayedPage.isShowingOwnSubmitButton());
        displayedPage.setWorkingBookingProperties(workingBookingProperties);
        validProperty.bind(displayedPage.validProperty());
        transitionPane.setReverse(index < displayedPageIndex);
        transitionPane.transitToContent(displayedPage.getView());
        displayedPageIndex = index;
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
    }

    private boolean isLastPage() {
        return displayedPageIndex == getPages().length - 1;
    }

    protected void updateNavigationBar() {
        if (navigationBar != null) {
            navigationBar.setTitleI18nKey(displayedPage.getTitleI18nKey());
            navigationBar.getBackButton().setDisable(displayedPageIndex == 0);
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
