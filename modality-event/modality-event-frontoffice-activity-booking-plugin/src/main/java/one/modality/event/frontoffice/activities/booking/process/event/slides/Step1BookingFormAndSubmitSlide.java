package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.FlipPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.animation.Animations;
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
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
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
import one.modality.ecommerce.frontoffice.bookingform.BookingForm;
import one.modality.ecommerce.frontoffice.bookingform.BookingFormActivityCallback;
import one.modality.event.frontoffice.activities.booking.process.event.EventBookingFormSettings;

/**
 * @author Bruno Salmon
 */
final class Step1BookingFormAndSubmitSlide extends StepSlide implements BookingFormActivityCallback {

    private final Button defaultSubmitButton = Bootstrap.largeSuccessButton(I18nControls.newButton(BaseI18nKeys.Submit));

    private final BooleanProperty bookingFormPersonToBookRequiredProperty = new SimpleBooleanProperty();
    private final BooleanProperty bookingFormShowDefaultSubmitButtonProperty = new SimpleBooleanProperty();
    private final BooleanProperty bookingFormDisableSubmitButtonProperty = new SimpleBooleanProperty();
    private final BooleanProperty bookingFormTransitingProperty = new SimpleBooleanProperty();
    private final BooleanProperty readyToSubmitBookingProperty = new SimpleBooleanProperty();
    // Node property that will be managed by the sub-router to mount the CheckoutAccountActivity (when routed)
    private final ObjectProperty<Node> embeddedLoginMountNodeProperty = new SimpleObjectProperty<>();
    private final VBox loginTopVBox = new VBox(10, Bootstrap.textPrimary(Bootstrap.strong(I18nControls.newLabel(BookingI18nKeys.LoginBeforeBooking))));
    private final Hyperlink orGuestLink = Bootstrap.textPrimary(I18nControls.newHyperlink(BookingI18nKeys.OrBookAsGuest));
    private final FlipPane loginGuestFlipPane = new FlipPane();
    private final MonoPane loginContent = new MonoPane();
    private final GuestPanel guestPanel = new GuestPanel();

    public Step1BookingFormAndSubmitSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
        mainVbox.setSpacing(100);
        defaultSubmitButton.setOnAction(event -> submitBooking());
        loginGuestFlipPane.setAlignment(Pos.TOP_CENTER);
        loginContent.contentProperty().bind(embeddedLoginMountNodeProperty); // managed by sub-router
        // Adding the container that will display the CheckoutAccountActivity (and eventually the login page before)
        BorderPane signInContainer = new BorderPane();
        loginTopVBox.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(loginTopVBox, new Insets(0, 0, 20, 0));
        signInContainer.setTop(loginTopVBox);
        signInContainer.setCenter(loginContent);
        loginGuestFlipPane.setFront(signInContainer);
        FXProperties.runNowAndOnPropertiesChange(this::updateLoginAndSubmitVisibility,
            FXPersonToBook.personToBookProperty(), bookingFormPersonToBookRequiredProperty, bookingFormShowDefaultSubmitButtonProperty, bookingFormDisableSubmitButtonProperty, bookingFormTransitingProperty);
        orGuestLink.setOnAction(e -> {
            loginGuestFlipPane.flipToBack();
            guestPanel.onShowing();
        });
        Hyperlink orAccountLink = Bootstrap.textPrimary(I18nControls.newHyperlink(BookingI18nKeys.OrBookUsingAccount));
        orAccountLink.setOnAction(e -> {
            loginGuestFlipPane.flipToFront();
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
            submitBooking();
        });
    }

    // Exposing accountMountNodeProperty for the sub-routing binding (done in SlideController)
    ObjectProperty<Node> accountMountNodeProperty() {
        return embeddedLoginMountNodeProperty;
    }

    @Override
    protected void buildSlideUi() {
        // The content is actually set by the setBookingForm()
    }

    void setBookingForm(BookingForm bookingForm) {
        bookingForm.setActivityCallback(this);

        Node bookingFormUi = bookingForm.buildUi();
        if (bookingFormUi == null) {
            mainVbox.getChildren().clear();
            return;
        }
        Collections.addIfNotContains("booking-form", bookingFormUi.getStyleClass());

        EventBookingFormSettings settings = (EventBookingFormSettings) bookingForm.getSettings();
        boolean bookAsAGuestAllowed = settings.bookAsAGuestAllowed();
        if (bookAsAGuestAllowed)
            loginGuestFlipPane.setBack(guestPanel.getContainer());
        Collections.addIfNotContainsOrRemove(loginTopVBox.getChildren(), bookAsAGuestAllowed, orGuestLink);

        // The booking form decides at which point to show the default submitButton
        showDefaultSubmitButton(false);
        setPersonToBookRequired(false);
        disableSubmitButton(false);
        bookingFormTransitingProperty.bind(bookingForm.transitingProperty());

        // and if it should be disabled or not, but in addition to that, we disable it if we show the login
        defaultSubmitButton.disableProperty().bind(readyToSubmitBookingProperty.not());
        guestPanel.getSubmitButton().disableProperty().bind(bookingFormDisableSubmitButtonProperty);

        mainVbox.getChildren().setAll(
            bookingFormUi,
            defaultSubmitButton
        );
    }

    private void updateLoginAndSubmitVisibility() {
        boolean transiting = bookingFormTransitingProperty.get();
        // Updating login visibility
        Person personToBook = FXPersonToBook.getPersonToBook();
        boolean showLogin = bookingFormPersonToBookRequiredProperty.get() && personToBook == null; // Means that the user is logged in with an account in Modality
        if (showLogin && loginContent.getContent() == null && !transiting) {
            WindowHistory.getProvider().push(CheckoutAccountRouting.getPath());
        }
        if (!transiting || showLogin)
            Layouts.setManagedAndVisibleProperties(loginGuestFlipPane, showLogin);
        Animations.animateProperty(loginGuestFlipPane.opacityProperty(), showLogin ? 1 : 0);
        if (!transiting) {
            Layouts.setManagedAndVisibleProperties(defaultSubmitButton, bookingFormShowDefaultSubmitButtonProperty.get() && !showLogin);
        }

        // Updating submit button visibility
        boolean readyToSubmitButton = !bookingFormDisableSubmitButtonProperty.get() && !showLogin;
        if (!readyToSubmitButton || !transiting)
            readyToSubmitBookingProperty.set(readyToSubmitButton);
        defaultSubmitButton.setDefaultButton(!showLogin);
    }

    @Override
    void turnOnWaitMode() {
        turnOnButtonWaitMode(defaultSubmitButton);
        guestPanel.turnOnButtonWaitMode();
    }

    @Override
    void turnOffWaitMode() {
        turnOffButtonWaitMode(defaultSubmitButton, BaseI18nKeys.Submit);
        guestPanel.turnOffButtonWaitMode();
    }

    // Callback (called by the booking form or its pages)

    @Override
    public void setPersonToBookRequired(boolean required) {
        bookingFormPersonToBookRequiredProperty.set(required);
    }

    @Override
    public void showDefaultSubmitButton(boolean show) {
        bookingFormShowDefaultSubmitButtonProperty.set(show);
    }

    @Override
    public void disableSubmitButton(boolean disable) {
        bookingFormDisableSubmitButtonProperty.set(disable);
    }

    @Override
    public Region getEmbeddedLoginNode() {
        return loginGuestFlipPane;
    }

    @Override
    public ObservableBooleanValue readyToSubmitBookingProperty() {
        return readyToSubmitBookingProperty;
    }

    private void submitBooking() {
        submitBooking(getWorkingBookingProperties().getBalance());
    }

    @Override
    public void submitBooking(int paymentDeposit) {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        WorkingBooking workingBooking = getWorkingBooking();
        // Three cases here:
        // 1) we pay an old balance with no new option, the currentBooking has no changes
        if (workingBooking.hasNoChanges()) {
            payOrThankYou(paymentDeposit);
        } else {
            // 2) the currentBooking has a new option
            turnOnWaitMode();
            // We look at the changes to fill the history
            WorkingBookingHistoryHelper historyHelper = new WorkingBookingHistoryHelper(workingBooking.getAttendancesAdded(true), workingBooking.getAttendancesRemoved(true));
            workingBooking.submitChanges(historyHelper.buildHistory())
                // Turning off the wait mode in all cases - Might be turned on again in success if payment is required
                .onComplete(ar -> UiScheduler.runInUiThread(this::turnOffWaitMode))
                .onFailure(throwable -> UiScheduler.runInUiThread(() -> {
                    displayErrorMessage(BookingI18nKeys.ErrorWhileInsertingBooking);
                    Console.log(throwable);
                }))
                .onSuccess(result -> UiScheduler.runInUiThread(() -> {
                    workingBookingProperties.setBookingReference(result.getDocumentRef());
                    payOrThankYou(paymentDeposit);
                }));
        }
    }

    private void payOrThankYou(int paymentDeposit) {
        // If a payment is required, we initiate the payment and display the payment slide
        if (paymentDeposit > 0) {
            initiateNewPaymentAndDisplayPaymentSlide(paymentDeposit); // will turn on wait mode again
        } else { // if no payment is required, we display the thank-you slide
            displayThankYouSlide();
        }
    }

}
