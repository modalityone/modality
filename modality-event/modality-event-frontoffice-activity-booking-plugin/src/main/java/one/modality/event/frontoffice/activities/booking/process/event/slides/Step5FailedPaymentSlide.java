package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

final class Step5FailedPaymentSlide extends StepSlide {

    private static final double MAX_SLIDE_WIDTH = 800;

    private final Button retryPayButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), "RetryPayment"));
    private final Button cancelButton = Bootstrap.largeDangerButton(I18nControls.bindI18nProperties(new Button(), "CancelBooking"));
    private Button pressedButton;

    Step5FailedPaymentSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
    }

    @Override
    void buildSlideUi() {
        mainVbox.setSpacing(10);
        mainVbox.setMaxWidth(MAX_SLIDE_WIDTH);

        Label title = Bootstrap.textDanger(Bootstrap.h3(I18nControls.bindI18nProperties(new Label(), "PaymentFailed")));
        title.setWrapText(true);
        VBox.setMargin(title, new Insets(50, 0, 0, 0));

        Label message = Bootstrap.textDanger(Bootstrap.h5(I18nControls.bindI18nProperties(new Label(), "PaymentFailedMessage")));
        message.setTextAlignment(TextAlignment.CENTER);
        message.setWrapText(true);
        VBox.setMargin(message, new Insets(50, 0, 50, 0));

        HBox buttonBar = new HBox(10, retryPayButton, cancelButton);
        buttonBar.setAlignment(Pos.CENTER);

        mainVbox.getChildren().setAll(title, message, buttonBar);

        retryPayButton.setOnAction(e -> {
            pressedButton = retryPayButton;
            initiateNewPaymentAndDisplayPaymentSlide();
        });
        cancelButton.setOnAction(e -> {
            pressedButton = cancelButton;
            cancelOrUncancelBookingAndDisplayNextSlide(true);
        });
    }

    @Override
    void turnOnWaitMode() {
        if (pressedButton == retryPayButton)
            turnOnButtonWaitMode(retryPayButton, cancelButton);
        else
            turnOnButtonWaitMode(cancelButton, retryPayButton);
    }

    @Override
    void turnOffWaitMode() {
        turnOffButtonWaitMode(retryPayButton, "RetryPayment");
        turnOffButtonWaitMode(cancelButton, "CancelBooking");
    }

}
