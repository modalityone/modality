package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.FlipPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.windowhistory.WindowHistory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Person;
import one.modality.ecommerce.client.workingbooking.FXPersonToBook;
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.client.workingbooking.WorkingBookingHistoryHelper;
import one.modality.ecommerce.client.workingbooking.WorkingBookingProperties;
import one.modality.event.frontoffice.activities.booking.BookingI18nKeys;
import one.modality.event.frontoffice.activities.booking.fx.FXGuestToBook;
import one.modality.event.frontoffice.activities.booking.process.account.CheckoutAccountRouting;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.BookingForm;

/**
 * @author Bruno Salmon
 */
final class Step1BookingFormAndSubmitSlide extends StepSlide {

    private final Button submitButton = Bootstrap.largeSuccessButton(I18nControls.newButton(BaseI18nKeys.Submit));

    private final BooleanProperty bookingFormShowLoginProperty = new SimpleBooleanProperty();
    private final BooleanProperty bookingFormShowSubmitButtonProperty = new SimpleBooleanProperty();
    // Node property that will be managed by the sub-router to mount the CheckoutAccountActivity (when routed)
    private final ObjectProperty<Node> checkoutAccountMountNodeProperty = new SimpleObjectProperty<>();
    private final VBox signIntopVBox = new VBox(10, Bootstrap.textPrimary(Bootstrap.strong(I18nControls.newLabel(BookingI18nKeys.LoginBeforeBooking))));
    private final Hyperlink orGuestLink = Bootstrap.textPrimary(I18nControls.newHyperlink(BookingI18nKeys.OrBookAsGuest));
    private final FlipPane flipPane = new FlipPane();
    MonoPane signInContent = new MonoPane();
    private final GuestPanel guestPanel = new GuestPanel();

    public Step1BookingFormAndSubmitSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
        mainVbox.setSpacing(100);
        submitButton.setOnAction(event -> submit());
        flipPane.setAlignment(Pos.TOP_CENTER);
        signInContent.contentProperty().bind(checkoutAccountMountNodeProperty); // managed by sub-router
        // Adding the container that will display the CheckoutAccountActivity (and eventually the login page before)
        BorderPane signInContainer = new BorderPane();
        signIntopVBox.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(signIntopVBox, new Insets(0, 0, 20, 0));
        signInContainer.setTop(signIntopVBox);
        signInContainer.setCenter(signInContent);
        flipPane.setFront(signInContainer);
        FXProperties.runNowAndOnPropertiesChange(this::updateLoginAndSubmitVisibility,
            FXPersonToBook.personToBookProperty(), bookingFormShowLoginProperty, bookingFormShowSubmitButtonProperty);
        orGuestLink.setOnAction(e -> {
            flipPane.flipToBack();
            guestPanel.onShowing();
        });
        Hyperlink orAccountLink = Bootstrap.textPrimary(I18nControls.newHyperlink(BookingI18nKeys.OrBookUsingAccount));
        orAccountLink.setOnAction(e -> {
            flipPane.flipToFront();
            guestPanel.onHiding();
        });
        guestPanel.addTopNode(orAccountLink);
        guestPanel.setOnSubmit(event -> {
            Document document = getWorkingBooking().getLastestDocumentAggregate().getDocument();
            document.setFirstName(guestPanel.getFirstName());
            document.setLastName(guestPanel.getLastName());
            document.setEmail(guestPanel.getEmail());
            document.setCountry(getEvent().getOrganization().getCountry());
            FXGuestToBook.setGuestToBook(document);
            submit();
        });
    }

    // Exposing accountMountNodeProperty for the sub-routing binding (done in SlideController)
    ObjectProperty<Node> accountMountNodeProperty() {
        return checkoutAccountMountNodeProperty;
    }

    @Override
    protected void buildSlideUi() {
        // The content is actually set by the setBookingForm()
    }

    void setBookingForm(BookingForm bookingForm) {
        Node bookingFormUi = bookingForm.buildUi();
        if (bookingFormUi == null) {
            mainVbox.getChildren().clear();
            return;
        }

        boolean bookAsAGuestAllowed = bookingForm.getSettings().isBookAsAGuestAllowed();
        if (bookAsAGuestAllowed)
            flipPane.setBack(guestPanel.getContainer());
        Collections.addIfNotContainsOrRemove(signIntopVBox.getChildren(), bookAsAGuestAllowed, orGuestLink);

        // The booking form decides at which point to show the submitButton
        bookingFormShowSubmitButtonProperty.bind(bookingForm.showSubmitButtonProperty());
        // and if it should be disabled or not
        submitButton.disableProperty().bind(bookingForm.disableSubmitButtonProperty());
        guestPanel.getSubmitButton().disableProperty().bind(bookingForm.disableSubmitButtonProperty());

        bookingFormShowLoginProperty.bind(bookingForm.showLoginProperty());

        mainVbox.getChildren().setAll(
            bookingFormUi,
            submitButton,
            flipPane
        );
    }

    private void updateLoginAndSubmitVisibility() {
        // Updating login visibility
        Person personToBook = FXPersonToBook.getPersonToBook();
        boolean showLogin = bookingFormShowLoginProperty.get() && personToBook == null; // Means that the user is logged in with an account in Modality
        if (showLogin && signInContent.getContent() == null) {
            WindowHistory.getProvider().push(CheckoutAccountRouting.getPath());
        }
        Layouts.setManagedAndVisibleProperties(flipPane, showLogin);

        // Updating submit button visibility
        Layouts.setManagedAndVisibleProperties(submitButton, bookingFormShowSubmitButtonProperty.get() && !showLogin);
        submitButton.setDefaultButton(showLogin);
    }

    boolean isEmpty() {
        return mainVbox.getChildren().isEmpty();
    }

    @Override
    void turnOnWaitMode() {
        turnOnButtonWaitMode(submitButton);
        guestPanel.turnOnButtonWaitMode();
    }

    @Override
    void turnOffWaitMode() {
        turnOffButtonWaitMode(submitButton, BaseI18nKeys.Submit);
        guestPanel.turnOffButtonWaitMode();
    }

    private void submit() {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        WorkingBooking workingBooking = getWorkingBooking();
        // Three cases here:
        // 1) we pay an old balance with no new option, the currentBooking has no changes
        if (workingBooking.hasNoChanges()) {
            initiateNewPaymentAndDisplayPaymentSlide(); // Will go to the payment page on success
        } else {
            // 2) the currentBooking has a new option
            turnOnWaitMode();
            // We look at the changes to fill the history
            WorkingBookingHistoryHelper historyHelper = new WorkingBookingHistoryHelper(workingBooking.getAttendanceAdded(), workingBooking.getAttendanceRemoved());
            workingBooking.submitChanges(historyHelper.buildHistory())
                // Turning off the wait mode in all cases - Might be turned on again in success if payment is required
                .onComplete(ar -> UiScheduler.runInUiThread(this::turnOffWaitMode))
                .onFailure(throwable -> UiScheduler.runInUiThread(() -> {
                    displayErrorMessage(BookingI18nKeys.ErrorWhileInsertingBooking);
                    Console.log(throwable);
                }))
                .onSuccess(result -> UiScheduler.runInUiThread(() -> {
                    workingBookingProperties.setBookingReference(result.getDocumentRef());
                    // If a payment is required, we initiate the payment and display the payment slide
                    if (workingBookingProperties.getBalance() > 0) {
                        initiateNewPaymentAndDisplayPaymentSlide(); // will turn on wait mode again
                    } else { // if no payment is required, we display the thank-you slide
                        displayThankYouSlide();
                    }
                }));
        }
    }

}
