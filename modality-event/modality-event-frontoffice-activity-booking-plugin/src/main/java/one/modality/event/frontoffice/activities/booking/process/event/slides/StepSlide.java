package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.operation.OperationUtil;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.geometry.Insets;
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
import one.modality.ecommerce.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.client.workingbooking.WorkingBookingProperties;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.client.ClientPaymentUtil;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.booking.BookableDatesUi;
import one.modality.event.frontoffice.activities.booking.BookingI18nKeys;
import one.modality.event.frontoffice.activities.booking.fx.FXGuestToBook;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
public abstract class StepSlide implements Supplier<Node> {

    private final BookEventActivity bookEventActivity;
    protected final VBox mainVbox = new VBox();

    protected StepSlide(BookEventActivity bookEventActivity) {
        this.bookEventActivity = bookEventActivity;
        mainVbox.setAlignment(Pos.TOP_CENTER);
        // Setting a good bottom margin (1/3 screen height), so bottom elements are not stuck at the booking of the screen
        mainVbox.setPadding(new Insets(0, 0, 150, 0));
        // Also a background is necessary for devices not supporting inverse clipping used in circle animation (ex: iPadOS)
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

    DocumentAggregate getDocumentAggregate() {
        return getWorkingBookingProperties().getDocumentAggregate();
    }

    protected Event getEvent() {
        return getWorkingBookingProperties().getEvent();
    }

    void displayBookSlide() {
        getBookEventActivity().displayBookSlide();
    }

    protected void displayCheckoutSlide() {
        getBookEventActivity().displayCheckoutSlide();
    }

    void displayPaymentSlide(WebPaymentForm webPaymentForm) {
        getBookEventActivity().displayPaymentSlide(webPaymentForm);
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

    void initiateNewPaymentAndDisplayPaymentSlide() {
        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        Object documentPrimaryKey = workingBookingProperties.getWorkingBooking().getDocumentPrimaryKey();
        turnOnWaitMode();
        PaymentService.initiatePayment(
                ClientPaymentUtil.createInitiatePaymentArgument(workingBookingProperties.getBalance(), documentPrimaryKey)
            )
            .onFailure(paymentResult -> UiScheduler.runInUiThread(() -> {
                turnOffWaitMode();
                displayErrorMessage(BookingI18nKeys.ErrorWhileInitiatingPayment);
                Console.log(paymentResult);
            }))
            .onSuccess(paymentResult -> UiScheduler.runInUiThread(() -> {
                turnOffWaitMode();
                HasPersonalDetails buyerDetails = FXUserPerson.getUserPerson();
                if (buyerDetails == null)
                    buyerDetails = FXGuestToBook.getGuestToBook();
                WebPaymentForm webPaymentForm = new WebPaymentForm(paymentResult, buyerDetails);
                displayPaymentSlide(webPaymentForm);
            }));
    }

    void cancelOrUncancelBookingAndDisplayNextSlide(boolean cancel) {
        WorkingBooking workingBooking = getWorkingBookingProperties().getWorkingBooking();
        if (cancel)
            workingBooking.cancelBooking();
        else
            workingBooking.uncancelBooking();
        turnOnWaitMode();
        workingBooking.submitChanges(cancel ? "Cancelled booking" : "Uncancelled booking")
            .onFailure(ex -> UiScheduler.runInUiThread(() -> {
                turnOffWaitMode();
                displayErrorMessage(ex.getMessage());
            }))
            .onSuccess(ignored -> {
                if (cancel)
                    displayCancellationSlide(new CancelPaymentResult(true));
                else
                    getBookEventActivity().loadBookingWithSamePolicy(false)
                        .onComplete(ar -> UiScheduler.runInUiThread(this::turnOffWaitMode));
            });
    }

    void turnOnWaitMode() {
    }

    void turnOffWaitMode() {
    }

    BookableDatesUi getBookableDatesUi() {
        return getBookEventActivity().getBookableDatesUi();
    }

    static void turnOnButtonWaitMode(Button... buttons) {
        OperationUtil.turnOnButtonsWaitMode(buttons);
    }

    static void turnOffButtonWaitMode(Button button, Object i18nKey) {
        OperationUtil.turnOffButtonsWaitMode(button); // but this doesn't reestablish the possible i18n graphic
        // So we reestablish it using i18n
        I18nControls.bindI18nGraphicProperty(button, i18nKey);
    }

    protected Button createPersonToBookButton() {
        return bookEventActivity.createPersonToBookButton();
    }

    public <T extends Labeled> T bindI18nEventExpression(T text, String eventExpression, Object... args) {
        return getBookEventActivity().bindI18nEventExpression(text, eventExpression, args);
    }

    public HtmlText bindI18nEventExpression(HtmlText text, String eventExpression, Object... args) {
        return getBookEventActivity().bindI18nEventExpression(text, eventExpression, args);
    }


}
