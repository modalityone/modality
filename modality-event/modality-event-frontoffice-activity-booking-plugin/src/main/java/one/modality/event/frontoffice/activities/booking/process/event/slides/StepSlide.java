package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.RecurringEventSchedule;
import one.modality.event.frontoffice.activities.booking.process.event.WorkingBookingProperties;

import java.util.function.Supplier;

abstract class StepSlide implements Supplier<Node> {

    private final BookEventActivity bookEventActivity;
    protected final VBox mainVbox = new VBox();

    StepSlide(BookEventActivity bookEventActivity) {
        this.bookEventActivity = bookEventActivity;
        mainVbox.setAlignment(Pos.TOP_CENTER);
        mainVbox.setPadding(new Insets(0, 30, 80, 30));
    }

    public Node get() {
        if (mainVbox.getChildren().isEmpty()) {
            buildSlideUi();
        }
        return mainVbox;
    }

    abstract void buildSlideUi();

    void reset() {
        mainVbox.getChildren().clear();
    }

    BookEventActivity getBookEventActivity() {
        return bookEventActivity;
    }

    WorkingBookingProperties getWorkingBookingProperties() {
        return getBookEventActivity().getWorkingBookingProperties();
    }

    void displayBookSlide() {
        getBookEventActivity().displayBookSlide();
    }

    void displayCheckoutSlide() {
        getBookEventActivity().displayCheckoutSlide();
    }

    void displayPaymentSlide(WebPaymentForm webPaymentForm) {
        getBookEventActivity().displayPaymentSlide(webPaymentForm);
    }

    void displayCancellationSlide() {
        getBookEventActivity().displayCancellationSlide();
    }

    void displayErrorMessage(String message) {
        getBookEventActivity().displayErrorMessage(message);
    }

    void displayThankYouSlide() {
        getBookEventActivity().displayThankYouSlide();
    }

    RecurringEventSchedule getRecurringEventSchedule() {
        return getBookEventActivity().getRecurringEventSchedule();
    }

    protected static void turnOnButtonWaitMode(Button... buttons) {
        OperationUtil.turnOnButtonsWaitMode(buttons);
    }

    protected static void turnOffButtonWaitMode(Button button, String i18nKey) {
        OperationUtil.turnOffButtonsWaitMode(button); // but this doesn't reestablish the possible i18n graphic
        // So we reestablish it using i18n
        I18nControls.bindI18nGraphicProperty(button, i18nKey);
    }
}
