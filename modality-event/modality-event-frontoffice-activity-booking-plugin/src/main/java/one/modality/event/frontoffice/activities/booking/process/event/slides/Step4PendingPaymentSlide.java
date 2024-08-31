package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.stack.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

final class Step4PendingPaymentSlide extends StepSlide {

    private static final double MAX_SLIDE_WIDTH = 800;

    Step4PendingPaymentSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
    }

    @Override
    void buildSlideUi() {
        mainVbox.setSpacing(10);
        mainVbox.setMaxWidth(MAX_SLIDE_WIDTH);

        Label title = Bootstrap.textWarning(Bootstrap.h3(new Label("Cancellation")));
        title.setWrapText(true);
        title.setPadding(new Insets(50, 0, 30, 0));

        HtmlText message = Bootstrap.h5(new HtmlText());
        I18n.bindI18nTextProperty(message.textProperty(), "PaymentPending");

        mainVbox.getChildren().setAll(title, message);
    }

}
