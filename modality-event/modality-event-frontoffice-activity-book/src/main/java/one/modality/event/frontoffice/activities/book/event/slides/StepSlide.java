package one.modality.event.frontoffice.activities.book.event.slides;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.console.Console;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.markers.HasPersonalDetails;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingform.GatewayPaymentForm;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.PaymentFormType;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.client.ClientPaymentUtil;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.frontoffice.activities.book.BookI18nKeys;
import one.modality.event.frontoffice.activities.book.event.BookEventActivity;
import one.modality.event.frontoffice.activities.book.fx.FXGuestToBook;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
public abstract class StepSlide implements Supplier<Node> {

    private static int LAST_PAYMENT_DEPOSIT;

    private final BookEventActivity bookEventActivity;
    protected final VBox mainVbox = new VBox();

    protected StepSlide(BookEventActivity bookEventActivity) {
        this.bookEventActivity = bookEventActivity;
        mainVbox.setAlignment(Pos.TOP_CENTER);
        // Also, a background is necessary for devices not supporting inverse clipping used in circle animation (ex: iPadOS)
        mainVbox.setBackground(Background.fill(Color.WHITE));
    }

    public Node get() {
        if (mainVbox.getChildren().isEmpty()) {
            buildSlideUi();
        }
        return mainVbox;
    }

    protected abstract void buildSlideUi();

    void reset() {
        mainVbox.getChildren().clear();
    }

    protected BookEventActivity getBookEventActivity() {
        return bookEventActivity;
    }

    protected WorkingBookingProperties getWorkingBookingProperties() {
        return getBookEventActivity().getWorkingBookingProperties();
    }

    WorkingBooking getWorkingBooking() {
        return getWorkingBookingProperties().getWorkingBooking();
    }

    protected Event getEvent() {
        return getWorkingBookingProperties().getEvent();
    }

    void displayBookSlide() {
        getBookEventActivity().displayBookSlide();
    }

    void displayPaymentSlide(GatewayPaymentForm gatewayPaymentForm) {
        getBookEventActivity().displayPaymentSlide(gatewayPaymentForm);
    }

    void displayPendingPaymentSlide() {
        getBookEventActivity().displayPendingPaymentSlide();
    }

    void displayFailedPaymentSlide() {
        getBookEventActivity().displayFailedPaymentSlide();
    }

    void displayCancellationSlide(CancelPaymentResult cancelPaymentResult) {
        getBookEventActivity().displayCancellationSlide(cancelPaymentResult);
    }

    void displayErrorMessage(Object messageI18nKey) {
        getBookEventActivity().displayErrorMessage(messageI18nKey);
    }

    void displayThankYouSlide() {
        getBookEventActivity().displayThankYouSlide();
    }

    void initiateNewPaymentAndDisplayPaymentSlide(int paymentDeposit, Consumer<GatewayPaymentForm> gatewayPaymentFormDisplayer) {
        LAST_PAYMENT_DEPOSIT = paymentDeposit;
        initiateNewPaymentAndDisplayPaymentSlide(gatewayPaymentFormDisplayer);
    }

    void initiateNewPaymentAndDisplayPaymentSlide(Consumer<GatewayPaymentForm> gatewayPaymentFormDisplayer) {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        Object documentPrimaryKey = workingBookingProperties.getWorkingBooking().getDocumentPrimaryKey();
        turnOnWaitMode();
        PaymentService.initiatePayment(
                ClientPaymentUtil.createInitiatePaymentArgument(
                    LAST_PAYMENT_DEPOSIT,
                    documentPrimaryKey,
                    PaymentFormType.REDIRECTED, // We were using EMBEDDED so far, now we try REDIRECTED
                    "/payment-return/:moneyTransferId",
                    "/payment-cancel/:moneyTransferId")
            )
            .inUiThread()
            .onFailure(paymentResult -> {
                turnOffWaitMode();
                displayErrorMessage(BookI18nKeys.ErrorWhileInitiatingPayment);
                Console.log(paymentResult);
            })
            .onSuccess(paymentResult -> {
                turnOffWaitMode();
                HasPersonalDetails buyerDetails = FXUserPerson.getUserPerson();
                if (buyerDetails == null)
                    buyerDetails = FXGuestToBook.getGuestToBook();
                WebPaymentForm webPaymentForm = new WebPaymentForm(paymentResult, buyerDetails);
                // If it's a redirected payment form, we just navigate to it
                if (webPaymentForm.isRedirectedPaymentForm())
                    webPaymentForm.navigateToRedirectedPaymentForm();
                    // No more slide to show, we just wait the redirected payment page to replace this app
                else { // Embedded payment form
                    // Creating and displaying the gateway payment form
                    GatewayPaymentForm gatewayPaymentForm = new ProvidedGatewayPaymentForm(webPaymentForm, this);
                    if (gatewayPaymentFormDisplayer != null)
                        gatewayPaymentFormDisplayer.accept(gatewayPaymentForm);
                    else
                        displayPaymentSlide(gatewayPaymentForm);
                }
            });
    }

    void cancelOrUncancelBookingAndDisplayNextSlide(boolean cancel) {
        WorkingBooking workingBooking = getWorkingBookingProperties().getWorkingBooking();
        if (cancel)
            workingBooking.cancelBooking();
        else
            workingBooking.uncancelBooking();
        turnOnWaitMode();
        workingBooking.submitChanges(cancel ? "Cancelled booking" : "Uncancelled booking")
            .inUiThread()
            .onFailure(ex -> {
                turnOffWaitMode();
                displayErrorMessage(ex.getMessage());
            })
            .onSuccess(ignored -> {
                if (cancel)
                    displayCancellationSlide(new CancelPaymentResult(true));
                else
                    getBookEventActivity().loadBookingWithSamePolicy(false)
                        .inUiThread()
                        .onComplete(ar -> turnOffWaitMode());
            });
    }

    void turnOnWaitMode() {
    }

    void turnOffWaitMode() {
    }

    static void turnOnButtonWaitMode(Button... buttons) {
        AsyncSpinner.displayButtonSpinner(buttons);
    }

    static void turnOffButtonWaitMode(Button button, Object i18nKey) {
        AsyncSpinner.hideButtonSpinner(button); // but this doesn't reestablish the possible i18n graphic
        // So we reestablish it using i18n
        I18nControls.bindI18nGraphicProperty(button, i18nKey);
    }

    public <T extends Labeled> T bindI18nEventExpression(T text, String eventExpression, Object... args) {
        return getBookEventActivity().bindI18nEventExpression(text, eventExpression, args);
    }

    public HtmlText bindI18nEventExpression(HtmlText text, String eventExpression, Object... args) {
        return getBookEventActivity().bindI18nEventExpression(text, eventExpression, args);
    }

}
