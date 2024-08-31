package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.WorkingBookingProperties;

final class Step3PaymentSlide extends StepSlide {

    private static final double MAX_SLIDE_WIDTH = 800;

    private final Label bookedEventTitleLabel = Bootstrap.textPrimary(Bootstrap.h4(new Label()));
    private final Label gatewayLogo = new Label();
    private final HtmlText paymentInformationHtmlText = Bootstrap.textPrimary(Bootstrap.h4(new HtmlText()));
    private final Button payButton = Bootstrap.largeSuccessButton(new Button());
    private final Button cancelButton = Bootstrap.largeSecondaryButton(new Button());
    private WebPaymentForm webPaymentForm;

    Step3PaymentSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
    }

    void setWebPaymentForm(WebPaymentForm webPaymentForm) {
        this.webPaymentForm = webPaymentForm;
    }

    @Override
    void buildSlideUi() {
        mainVbox.setMaxWidth(MAX_SLIDE_WIDTH);
        mainVbox.setAlignment(Pos.CENTER_LEFT);

        VBox.setMargin(bookedEventTitleLabel, new Insets(20, 0, 0, 0));

        I18n.bindI18nTextProperty(paymentInformationHtmlText.textProperty(), "PaymentInformation", webPaymentForm.getGatewayName());
        paymentInformationHtmlText.getStyleClass().add("subtitle-grey");
        VBox.setMargin(paymentInformationHtmlText, new Insets(10, 0, 20, 0));

        I18nControls.bindI18nProperties(gatewayLogo, webPaymentForm.getGatewayName());
        VBox.setMargin(gatewayLogo, new Insets(10, 0, 20, 0));

        mainVbox.getChildren().setAll(
            bookedEventTitleLabel,
            paymentInformationHtmlText,
            gatewayLogo
        );

        bookedEventTitleLabel.setWrapText(true);
        Region paymentRegion = webPaymentForm.buildPaymentForm();
        mainVbox.getChildren().add(paymentRegion);
        if (webPaymentForm.isSandbox()) {
            mainVbox.getChildren().add(webPaymentForm.createSandboxBar());
        }

        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        I18nControls.bindI18nProperties(new Button(), "Pay", workingBookingProperties.formattedBalanceProperty());
        I18nControls.bindI18nProperties(new Button(), "Cancel");
        payButton.setDefaultButton(true);
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (webPaymentForm.isUserInteractionAllowed()) {
                turnOffWaitMode();
            } else {
                turnOnWaitMode();
            }
        }, webPaymentForm.userInteractionAllowedProperty());
        payButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        payButton.setOnAction(e -> webPaymentForm.pay());
        cancelButton.setOnAction(e -> {
            webPaymentForm.cancelPayment()
                .onComplete(ar -> UiScheduler.runInUiThread(this::displayCancellationSlide));
        });
        FlexPane buttonBar = new FlexPane(payButton, cancelButton);
        buttonBar.setHorizontalSpace(10);
        VBox.setMargin(buttonBar, new Insets(10, 0, 10, 0));
        mainVbox.getChildren().add(buttonBar);
        bookedEventTitleLabel.setText(getEvent().getName() + " |\u00A0Total\u00A0booking\u00A0price: " + workingBookingProperties.formattedBalanceProperty().getValue());
        webPaymentForm
            .setOnLoadFailure(errorMsg -> {
                displayErrorMessage("ErrorWhileLoadingPaymentForm");
                Console.log(errorMsg);
            })
            .setOnInitFailure(errorMsg -> {
                displayErrorMessage("ErrorWhileInitializingHTMLPaymentForm");
                Console.log(errorMsg);
            })
            .setOnVerificationFailure(errorMsg -> {
                displayErrorMessage("ErrorPaymentGatewayFailure");
                Console.log(errorMsg);
            })
            .setOnPaymentFailure(errorMsg -> {
                displayErrorMessage("ErrorPaymentModalityFailure");
                Console.log(errorMsg);
            })
            .setOnPaymentCompletion(status -> {
                if (status.isPending()) {
                    displayPendingPaymentSlide();
                } if (status.isSuccessful()) {
                    displayThankYouSlide();
                } else { // failed payment
                    displayFailedPaymentSlide();
                }
            });
    }

    @Override
    void turnOnWaitMode() {
        turnOnButtonWaitMode(payButton, cancelButton);
    }

    @Override
    void turnOffWaitMode() {
        turnOffButtonWaitMode(payButton, "Pay");
        turnOffButtonWaitMode(cancelButton, "Cancel");
    }
}
