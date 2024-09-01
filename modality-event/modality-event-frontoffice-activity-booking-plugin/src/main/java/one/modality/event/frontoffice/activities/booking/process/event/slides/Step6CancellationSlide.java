package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.event.frontoffice.activities.booking.WorkingBooking;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

final class Step6CancellationSlide extends StepSlide {

    private static final double MAX_SLIDE_WIDTH = 800;

    private final Label cancellationMessage = Bootstrap.h5(new Label());
    private final Button button = Bootstrap.largeDangerButton(new Button());

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

        cancellationMessage.setTextAlignment(TextAlignment.CENTER);
        cancellationMessage.setWrapText(true);
        VBox.setMargin(cancellationMessage, new Insets(50, 0, 50, 0));

        button.setOnAction(e -> {
            turnOnWaitMode();
            // Booking cancelled => un-cancel booking
            if (cancelPaymentResult.isBookingCancelled()) {
                WorkingBooking workingBooking = getWorkingBookingProperties().getWorkingBooking();
                workingBooking.uncancelBooking();
                workingBooking.submitChanges("Uncancelled booking")
                    .onFailure(ex -> {
                        turnOffWaitMode();
                        displayErrorMessage(ex.getMessage());
                    })
                    .onSuccess(ignored -> getBookEventActivity().loadBookingWithSamePolicy(false)
                        .onComplete(ar -> turnOffWaitMode()));
            } else {
                getBookEventActivity().loadBookingWithSamePolicy(false)
                    .onComplete(ar -> turnOffWaitMode());
            }
        });

        mainVbox.getChildren().setAll(title, cancellationMessage, button);
    }

    @Override
    void turnOnWaitMode() {
        UiScheduler.runInUiThread(() -> turnOnButtonWaitMode(button));
    }

    @Override
    void turnOffWaitMode() {
        UiScheduler.runInUiThread(() ->
            turnOffButtonWaitMode(button, cancelPaymentResult.isBookingCancelled() ? "UncancelBooking" : "BookAgain"));
    }

    public void setCancelPaymentResult(CancelPaymentResult cancelPaymentResult) {
        this.cancelPaymentResult = cancelPaymentResult;
        if (cancelPaymentResult.isBookingCancelled()) {
            I18nControls.bindI18nProperties(cancellationMessage, "PaymentAndBookingCancelled");
            I18nControls.bindI18nProperties(button, "UncancelBooking");
        } else {
            I18nControls.bindI18nProperties(cancellationMessage, "PaymentCancelled");
            I18nControls.bindI18nProperties(button, "BookAgain");
        }
    }
}
