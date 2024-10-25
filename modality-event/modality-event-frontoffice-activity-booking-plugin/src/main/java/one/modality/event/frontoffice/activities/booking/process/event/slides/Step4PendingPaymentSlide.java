package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import one.modality.event.frontoffice.activities.booking.BookingI18nKeys;
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

        Label title = Bootstrap.textWarning(Bootstrap.h3(new Label(BookingI18nKeys.PaymentPending)));
        title.setWrapText(true);
        title.setPadding(new Insets(50, 0, 30, 0));

        Label message = Bootstrap.textDanger(Bootstrap.h5(I18nControls.bindI18nProperties(new Label(), BookingI18nKeys.PaymentPendingMessage)));

        mainVbox.getChildren().setAll(title, message);
    }

}
