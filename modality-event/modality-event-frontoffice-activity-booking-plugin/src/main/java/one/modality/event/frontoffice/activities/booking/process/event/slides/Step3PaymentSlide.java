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
import one.modality.base.client.i18n.ModalityI18nKeys;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.frontoffice.activities.booking.BookingI18nKeys;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.ecommerce.client.workingbooking.WorkingBookingProperties;

final class Step3PaymentSlide extends StepSlide {

    private static final double MAX_SLIDE_WIDTH = 800;

    private final Label bookedEventTitleLabel = Bootstrap.textPrimary(Bootstrap.h4(new Label()));
    private final Label gatewayLogo = new Label();
    private final HtmlText paymentInformationHtmlText = Bootstrap.textPrimary(Bootstrap.h4(new HtmlText()));
    private final Button payButton = Bootstrap.largeSuccessButton(new Button());
    private final Button cancelButton = Bootstrap.largeSecondaryButton(new Button());
    private Button pressedButton;
    private WebPaymentForm webPaymentForm;

    Step3PaymentSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
    }

    void setWebPaymentForm(WebPaymentForm webPaymentForm) {
        this.webPaymentForm = webPaymentForm;
        mainVbox.getChildren().clear(); //
    }

    @Override
    public void buildSlideUi() {
        mainVbox.setMaxWidth(MAX_SLIDE_WIDTH);
        mainVbox.setAlignment(Pos.CENTER_LEFT);

        WorkingBookingProperties workingBookingProperties = getWorkingBookingProperties();
        bindI18nEventExpression(bookedEventTitleLabel, "i18n(this) + '[" + BookingI18nKeys.TotalBookingPrice0 + "]'", workingBookingProperties.formattedBalanceProperty());
        bookedEventTitleLabel.setWrapText(true);
        VBox.setMargin(bookedEventTitleLabel, new Insets(20, 0, 0, 0));

        I18n.bindI18nTextProperty(paymentInformationHtmlText.textProperty(), BookingI18nKeys.PaymentInformation0, webPaymentForm.getGatewayName());
        paymentInformationHtmlText.getStyleClass().add("subtitle-grey");
        VBox.setMargin(paymentInformationHtmlText, new Insets(10, 0, 20, 0));

        I18nControls.bindI18nProperties(gatewayLogo, webPaymentForm.getGatewayName());
        VBox.setMargin(gatewayLogo, new Insets(10, 0, 20, 0));

        Region paymentRegion = webPaymentForm.buildPaymentForm();

        mainVbox.getChildren().setAll(
            bookedEventTitleLabel,
            paymentInformationHtmlText,
            gatewayLogo,
            paymentRegion
        );

        if (webPaymentForm.isSandbox()) {
            mainVbox.getChildren().add(webPaymentForm.createSandboxBar());
        }

        I18nControls.bindI18nProperties(payButton, BookingI18nKeys.Pay0, workingBookingProperties.formattedBalanceProperty());
        I18nControls.bindI18nProperties(cancelButton, ModalityI18nKeys.Cancel);
        payButton.setDefaultButton(true);
        FXProperties.runNowAndOnPropertyChange(userInteractionAllowed -> {
            if (userInteractionAllowed) {
                turnOffWaitMode();
            } else {
                turnOnWaitMode();
            }
        }, webPaymentForm.userInteractionAllowedProperty());
        payButton.setOnAction(e -> {
            pressedButton = payButton;
            webPaymentForm.pay();
        });
        cancelButton.setOnAction(e -> {
            pressedButton = cancelButton;
            webPaymentForm.cancelPayment()
                .onComplete(ar -> UiScheduler.runInUiThread(() -> {
                    if (ar.failed())
                        displayErrorMessage(ar.cause().getMessage());
                    else {
                        displayCancellationSlide(ar.result());
                    }
                }));
        });
        payButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        FlexPane buttonBar = new FlexPane(payButton, cancelButton);
        buttonBar.setHorizontalSpace(10);
        VBox.setMargin(buttonBar, new Insets(10, 0, 10, 0));
        mainVbox.getChildren().add(buttonBar);

        webPaymentForm
            .setOnLoadFailure(errorMsg -> {
                displayErrorMessage(BookingI18nKeys.ErrorWhileLoadingPaymentForm);
                Console.log(errorMsg);
            })
            .setOnInitFailure(errorMsg -> {
                displayErrorMessage(BookingI18nKeys.ErrorWhileInitializingHTMLPaymentForm);
                Console.log(errorMsg);
            })
            .setOnVerificationFailure(errorMsg -> {
                displayErrorMessage(BookingI18nKeys.ErrorPaymentGatewayFailure);
                Console.log(errorMsg);
            })
            .setOnPaymentFailure(errorMsg -> {
                displayErrorMessage(BookingI18nKeys.ErrorPaymentModalityFailure);
                Console.log(errorMsg);
            })
            .setOnPaymentCompletion(status -> {
                if (status.isPending()) {
                    displayPendingPaymentSlide();
                } else if (status.isSuccessful()) {
                    displayThankYouSlide();
                } else { // failed payment
                    displayFailedPaymentSlide();
                }
            });
    }

    @Override
    void turnOnWaitMode() {
        if (pressedButton == payButton)
            turnOnButtonWaitMode(payButton, cancelButton);
        else
            turnOnButtonWaitMode(cancelButton, payButton);
    }

    @Override
    void turnOffWaitMode() {
        turnOffButtonWaitMode(payButton, EcommerceI18nKeys.Pay);
        turnOffButtonWaitMode(cancelButton, ModalityI18nKeys.Cancel);
    }
}
