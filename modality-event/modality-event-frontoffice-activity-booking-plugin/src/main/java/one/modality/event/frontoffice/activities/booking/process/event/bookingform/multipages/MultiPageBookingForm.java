package one.modality.event.frontoffice.activities.booking.process.event.bookingform.multipages;

import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.BookingFormBase;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.BookingFormSettings;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.PriceBar;

/**
 * @author Bruno Salmon
 */
public abstract class MultiPageBookingForm extends BookingFormBase {

    private static final double MAX_WIDTH = 800;

    private final NavigationBar navigationBar;
    private final TransitionPane transitionPane = new TransitionPane();
    private int currentFamilyOptionsViewIndex;

    public MultiPageBookingForm(BookEventActivity activity, BookingFormSettings settings) {
        super(activity, settings);
        Layouts.setFixedWidth(transitionPane, MAX_WIDTH);
        navigationBar = settings.showNavigationBar() ? new NavigationBar() : null;
    }

    protected abstract BookingFormPage[] getPages();

    @Override
    public Node buildUi() {
        BorderPane container = new BorderPane(transitionPane);
        if (navigationBar != null) {
            container.setTop(navigationBar.getView());
            navigationBar.getBackButton().setOnMouseClicked(e -> navigateToPreviousPage());
            navigationBar.getNextButton().setOnMouseClicked(e -> navigateToNextPage());
        }
        if (settings.showNavigationBar())
            container.setBottom(new PriceBar(activity.getWorkingBookingProperties()).getView());
        container.getStyleClass().add("online-festival-booking-form");
        container.setMaxWidth(MAX_WIDTH);
        return container;
    }

    @Override
    public void onWorkingBookingLoaded() {
        //bookWholeEvent();
        navigateToPage(0);
    }

    public void navigateToPreviousPage() {
        navigateToPage(currentFamilyOptionsViewIndex - 1);
    }

    public void navigateToNextPage() {
        BookingFormPage[] pages = getPages();
        BookingFormPage bookingFormPage = pages[currentFamilyOptionsViewIndex];
        if (bookingFormPage.isValid() && currentFamilyOptionsViewIndex < pages.length - 1)
            navigateToPage(currentFamilyOptionsViewIndex + 1);
    }

    private void navigateToPage(int index) {
        BookingFormPage[] pages = getPages();
        BookingFormPage bookingFormPage = pages[index];
        bookingFormPage.setWorkingBooking(activity.getWorkingBooking());
        transitionPane.setReverse(index < currentFamilyOptionsViewIndex);
        transitionPane.transitToContent(bookingFormPage.getView());
        currentFamilyOptionsViewIndex = index;
        if (navigationBar != null) {
            navigationBar.setTitleI18nKey(bookingFormPage.getTitleI18nKey());
            navigationBar.getBackButton().setDisable(index == 0);
            //navigationBar.getNextButton().setDisable(index == familyOptionsViews.length - 1);
        }
        disableSubmitButtonProperty.bind(FXProperties.not(bookingFormPage.validProperty()));
        updateShowLogin();
        updateShowSubmitButton();
    }

    protected void updateShowLogin() {
        setShowLogin(currentFamilyOptionsViewIndex == getPages().length - 1);
    }

    protected void updateShowSubmitButton() {
        setShowSubmitButton(currentFamilyOptionsViewIndex == getPages().length - 1);
    }

}
