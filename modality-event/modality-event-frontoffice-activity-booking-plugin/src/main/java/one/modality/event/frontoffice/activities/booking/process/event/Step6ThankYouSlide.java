package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

class Step6ThankYouSlide extends StepSlide{

    public Step6ThankYouSlide(SlideController control, BookEventData bed) {
        super(control, bed);
        controller.setStep6ThankYouSlide(this);

    }


    public void buildUi() {
        mainVbox.setAlignment(Pos.TOP_CENTER);
        mainVbox.setSpacing(40);
        String headerImageUrl = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.event.frontoffice.activity.bookevent").getString("headerImageUrl");
        ImageView headerImageView = ImageStore.createImageView(headerImageUrl);
        ScalePane headerImageScalePane = new ScalePane(headerImageView);
        headerImageScalePane.setPadding(new Insets(30,0,50,0));

        Label bookingConfirmedLabel = Bootstrap.textSuccess(I18nControls.bindI18nProperties(new Label(), "BookingSubmitted"));
        bookingConfirmedLabel.setContentDisplay(ContentDisplay.TOP);
        bookingConfirmedLabel.setGraphicTextGap(10);

        Label thankYouLabel = Bootstrap.textSecondary(I18nControls.bindI18nProperties(new Label(), "ThankYouForBooking"));
        thankYouLabel.setWrapText(true);
        thankYouLabel.setTextAlignment(TextAlignment.CENTER);
        MonoPane thankYouPane = new MonoPane(thankYouLabel);
        thankYouPane.setAlignment(Pos.CENTER);
        VBox.setMargin(thankYouPane, new Insets(20,0,0,50));
        //thankYouHBox.maxWidthProperty().bind(headerImageScalePane.widthProperty().multiply(0.8));
        thankYouPane.maxWidthProperty().bind(FXProperties.compute(headerImageScalePane.widthProperty(), w -> w.doubleValue() * 0.8));

        Label bookingNumber = I18nControls.bindI18nProperties(new Label(), "BookingNumber",bookEventData.bookingReferenceProperty());
        bookingNumber.setWrapText(true);
        bookingNumber.setTextAlignment(TextAlignment.CENTER);
        MonoPane bookingNumberPane = new MonoPane(bookingNumber);
        bookingNumberPane.setAlignment(Pos.CENTER);
        VBox.setMargin(bookingNumberPane, new Insets(20,0,0,50));
        mainVbox.getChildren().setAll(headerImageScalePane,bookingConfirmedLabel,thankYouPane,bookingNumberPane);
    }
}
