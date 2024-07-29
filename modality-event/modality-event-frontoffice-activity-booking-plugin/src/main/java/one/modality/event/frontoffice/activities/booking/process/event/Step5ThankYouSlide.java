package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

final class Step5ThankYouSlide extends StepSlide {

    Step5ThankYouSlide(SlideController control) {
        super(control);
    }

    void buildUi() {
        mainVbox.setAlignment(Pos.TOP_CENTER);
        mainVbox.setSpacing(40);
        String headerImageUrl = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.event.frontoffice.activity.bookevent").getString("headerImageUrl");
        ImageView headerImageView = ImageStore.createImageView(headerImageUrl);
        ScalePane headerImageScalePane = new ScalePane(headerImageView);
        headerImageScalePane.setPadding(new Insets(30,0,50,0));

        Label bookingConfirmedLabel = Bootstrap.textSuccess(Bootstrap.h3(I18nControls.bindI18nProperties(new Label(), "BookingSubmitted")));
        bookingConfirmedLabel.setContentDisplay(ContentDisplay.TOP);
        bookingConfirmedLabel.setGraphicTextGap(10);

        Label thankYouLabel = Bootstrap.textSecondary(Bootstrap.h4(I18nControls.bindI18nProperties(new Label(), "ThankYouForBooking")));
        thankYouLabel.setWrapText(true);
        thankYouLabel.setTextAlignment(TextAlignment.CENTER);
        VBox.setMargin(thankYouLabel, new Insets(20,0,50,0));

        Label bookingNumber = I18nControls.bindI18nProperties(new Label(), "BookingNumber", controller.getBookEventData().bookingReferenceProperty());
        bookingNumber.setWrapText(true);
        bookingNumber.setTextAlignment(TextAlignment.CENTER);
        VBox.setMargin(bookingNumber, new Insets(20,0,50,0));
        mainVbox.getChildren().setAll(
                headerImageScalePane,
                bookingConfirmedLabel,
                thankYouLabel,
                bookingNumber);
    }
}
