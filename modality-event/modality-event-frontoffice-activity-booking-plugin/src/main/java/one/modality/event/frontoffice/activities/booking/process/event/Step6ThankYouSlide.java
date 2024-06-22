package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

public class Step6ThankYouSlide extends StepSlide{

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
        mainVbox.getChildren().add(headerImageScalePane);

        Label bookingConfirmedLabel = I18nControls.bindI18nProperties(new Label(), "BookingSubmitted");
        bookingConfirmedLabel.setContentDisplay(ContentDisplay.TOP);
        bookingConfirmedLabel.setGraphicTextGap(10);
        //  bookingConfirmedLabel.getGraphic().getStyleClass().add("success-text");
        bookingConfirmedLabel.getStyleClass().addAll("success-text","emphasize");
        mainVbox.getChildren().add(bookingConfirmedLabel);

        HBox thankYouHBox = new HBox();
        thankYouHBox.setAlignment(Pos.CENTER);
        Label thankYouLabel = I18nControls.bindI18nProperties(new Label(), "ThankYouForBooking");
        thankYouLabel.setWrapText(true);
        thankYouLabel.setTextAlignment(TextAlignment.CENTER);
        thankYouLabel.getStyleClass().add("secondary-text");

        //thankYouHBox.maxWidthProperty().bind(headerImageScalePane.widthProperty().multiply(0.8));
        thankYouHBox.maxWidthProperty().bind(FXProperties.compute(headerImageScalePane.widthProperty(), w -> w.doubleValue() * 0.8));

        thankYouHBox.getChildren().add(thankYouLabel);
        mainVbox.getChildren().add(thankYouHBox);

        HBox bookingNumberHBox = new HBox();
        bookingNumberHBox.setAlignment(Pos.CENTER);
        Label bookingNumber = I18nControls.bindI18nProperties(new Label(), "BookingNumber",bookEventData.bookingNumberProperty());
        bookingNumber.setWrapText(true);
        bookingNumber.setTextAlignment(TextAlignment.CENTER);
      //  bookingNumber.setMaxWidth(MAX_WIDTH*0.8);
        bookingNumberHBox.getChildren().add(bookingNumber);
        mainVbox.getChildren().add(bookingNumberHBox);
    }
}
