package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.event.frontoffice.activities.booking.BookingI18nKeys;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

final class Step6CancellationSlide extends StepSlide {

    private static final double MAX_SLIDE_WIDTH = 800;

    private final Label message = Bootstrap.h5(new Label());
    private final Button button = Bootstrap.largeSuccessButton(new Button());

    private CancelPaymentResult cancelPaymentResult;

    Step6CancellationSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
    }

    @Override
    void buildSlideUi() {
        mainVbox.setSpacing(10);
        mainVbox.setMaxWidth(MAX_SLIDE_WIDTH);

        Label title = Bootstrap.textWarning(Bootstrap.h3(new Label("Cancellation")));
        title.setWrapText(true);
        VBox.setMargin(title, new Insets(50, 0, 0, 0));

        message.setTextAlignment(TextAlignment.CENTER);
        message.setWrapText(true);
        VBox.setMargin(message, new Insets(50, 0, 50, 0));

        button.setOnAction(e -> {
            // Booking cancelled => un-cancel booking
            if (cancelPaymentResult.isBookingCancelled()) {
                cancelOrUncancelBookingAndDisplayNextSlide(false);
            } else {
                turnOnWaitMode();
                getBookEventActivity().loadBookingWithSamePolicy(false)
                    .onComplete(ar -> turnOffWaitMode());
            }
        });

        mainVbox.getChildren().setAll(title, message, button);
    }

    @Override
    void turnOnWaitMode() {
        turnOnButtonWaitMode(button);
    }

    @Override
    void turnOffWaitMode() {
        turnOffButtonWaitMode(button, cancelPaymentResult.isBookingCancelled() ? "UncancelBooking" : "BookAgain");
    }

    public void setCancelPaymentResult(CancelPaymentResult cancelPaymentResult) {
        this.cancelPaymentResult = cancelPaymentResult;
        if (cancelPaymentResult.isBookingCancelled()) {
            I18nControls.bindI18nProperties(message, BookingI18nKeys.PaymentAndBookingCancelled);
            I18nControls.bindI18nProperties(button, BookingI18nKeys.UncancelBooking);
        } else {
            I18nControls.bindI18nProperties(message, BookingI18nKeys.PaymentCancelled);
            I18nControls.bindI18nProperties(button, BookingI18nKeys.BookAgain);
        }
    }
}
