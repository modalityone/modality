package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

final class Step5FailedPaymentSlide extends StepSlide {

    private static final double MAX_SLIDE_WIDTH = 800;

    private final Button retryPayButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), "RetryPayment"));

    Step5FailedPaymentSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
    }

    @Override
    void buildSlideUi() {
        mainVbox.setSpacing(10);
        mainVbox.setMaxWidth(MAX_SLIDE_WIDTH);

        Label title = Bootstrap.textDanger(Bootstrap.h3(new Label("Cancellation")));
        title.setWrapText(true);
        title.setPadding(new Insets(50, 0, 30, 0));

        HtmlText message = Bootstrap.h5(new HtmlText());
        I18n.bindI18nTextProperty(message.textProperty(), "PaymentFailed");

        retryPayButton.setOnAction(e -> initiateNewPaymentAndDisplayPaymentSlide());

        mainVbox.getChildren().setAll(title, message, retryPayButton);
    }

    @Override
    void turnOnWaitMode() {
        turnOnButtonWaitMode(retryPayButton);
    }
    @Override
    void turnOffWaitMode() {
        turnOffButtonWaitMode(retryPayButton, "RetryPayment");
    }

}
