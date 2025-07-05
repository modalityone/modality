package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.event.frontoffice.activities.booking.BookingI18nKeys;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

/**
 * @author Bruno Salmon
 */
final class StepCThankYouSlide extends StepSlide {

    private static final double MAX_PAGE_WITH = 800;

    StepCThankYouSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
    }

    @Override
    public void buildSlideUi() {
        String headerImageUrl = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.event.frontoffice.activity.bookevent").getString("headerImageUrl");
        ImageView headerImageView = ImageStore.createImageView(headerImageUrl);
        ScalePane headerImageScalePane = new ScalePane(headerImageView);
        headerImageScalePane.setPadding(new Insets(30,0,50,0));

        Label bookingConfirmedLabel = Bootstrap.textSuccess(Bootstrap.h3(I18nControls.newLabel(BookingI18nKeys.BookingSubmitted)));
        bookingConfirmedLabel.setContentDisplay(ContentDisplay.TOP);
        bookingConfirmedLabel.setGraphicTextGap(10);

        Label thankYouLabel = Bootstrap.textSecondary(Bootstrap.h4(I18nControls.newLabel(BookingI18nKeys.ThankYouForBooking)));
        thankYouLabel.setWrapText(true);
        thankYouLabel.setTextAlignment(TextAlignment.CENTER);
        VBox.setMargin(thankYouLabel, new Insets(20,0,50,0));

        Label bookingNumber = I18nControls.newLabel(BookingI18nKeys.BookingNumber1, getWorkingBookingProperties().bookingReferenceProperty());
        bookingNumber.setWrapText(true);
        bookingNumber.setTextAlignment(TextAlignment.CENTER);

        mainVbox.getChildren().setAll(
                headerImageScalePane,
                bookingConfirmedLabel,
                thankYouLabel,
                bookingNumber);
        mainVbox.setMaxWidth(MAX_PAGE_WITH);
        mainVbox.setSpacing(40);
    }
}
