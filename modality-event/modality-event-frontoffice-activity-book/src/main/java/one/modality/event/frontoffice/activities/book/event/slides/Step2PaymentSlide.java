package one.modality.event.frontoffice.activities.book.event.slides;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingform.GatewayPaymentForm;
import one.modality.event.frontoffice.activities.book.BookI18nKeys;
import one.modality.event.frontoffice.activities.book.event.BookEventActivity;

/**
 * @author Bruno Salmon
 */
final class Step2PaymentSlide extends StepSlide {

    private static final double MAX_SLIDE_WIDTH = 800;

    private final Label bookedEventTitleLabel = Bootstrap.textPrimary(Bootstrap.h4(new Label()));
    private final HtmlText paymentInformationHtmlText = Bootstrap.textPrimary(Bootstrap.h4(new HtmlText()));

    Step2PaymentSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
    }

    @Override
    public void buildSlideUi() {
        mainVbox.setMaxWidth(MAX_SLIDE_WIDTH);
        mainVbox.setAlignment(Pos.CENTER_LEFT);

        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        bindI18nEventExpression(bookedEventTitleLabel, "i18n(this) + ' [" + BookI18nKeys.TotalBookingPrice1 + "]'", workingBookingProperties.formattedBalanceProperty());
        bookedEventTitleLabel.setWrapText(true);
        VBox.setMargin(bookedEventTitleLabel, new Insets(20, 0, 0, 0));

        paymentInformationHtmlText.getStyleClass().add("subtitle-grey");
        VBox.setMargin(paymentInformationHtmlText, new Insets(10, 0, 20, 0));
    }

    void setWebPaymentForm(GatewayPaymentForm gatewayPaymentForm) {
        I18n.bindI18nTextProperty(paymentInformationHtmlText.textProperty(), BookI18nKeys.PaymentInformation1, gatewayPaymentForm.getGatewayName());
        mainVbox.getChildren().setAll(
            bookedEventTitleLabel,
            paymentInformationHtmlText,
            gatewayPaymentForm.getView()
        );
    }
}
