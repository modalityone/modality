package one.modality.event.frontoffice.activities.book.event.slides;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.AsyncResult;
import dev.webfx.platform.console.Console;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.booking.frontoffice.bookingform.GatewayPaymentForm;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.CompletePaymentResult;
import one.modality.ecommerce.payment.PaymentStatus;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.frontoffice.activities.book.BookI18nKeys;

import java.util.function.Consumer;

import static one.modality.event.frontoffice.activities.book.event.slides.StepSlide.turnOffButtonWaitMode;
import static one.modality.event.frontoffice.activities.book.event.slides.StepSlide.turnOnButtonWaitMode;

/**
 * @author Bruno Salmon
 */
public final class ProvidedGatewayPaymentForm implements GatewayPaymentForm {

    private final String gatewayName;
    private final Button payButton = Bootstrap.largeSuccessButton(new Button());
    private final Button cancelButton = Bootstrap.largeSecondaryButton(I18nControls.newButton(BaseI18nKeys.Cancel));
    private final VBox mainVbox;
    private Button pressedButton;
    private Consumer<AsyncResult<CancelPaymentResult>> cancelPaymentResultHandler;

    public ProvidedGatewayPaymentForm(WebPaymentForm webPaymentForm, Event event, Consumer<Object> errorConsumer, Consumer<CancelPaymentResult> cancelConsumer, Consumer<PaymentStatus> successConsumer) {
        gatewayName = webPaymentForm.getGatewayName();

        Label gatewayLogo = new Label();
        I18nControls.bindI18nProperties(gatewayLogo, webPaymentForm.getGatewayName());
        //VBox.setMargin(gatewayLogo, new Insets(10, 0, 20, 0));

        I18nControls.bindI18nProperties(payButton, BookI18nKeys.Pay1, EventPriceFormatter.formatWithCurrency(webPaymentForm.getAmount(), event));
        Layouts.setManagedAndVisibleProperties(payButton, !webPaymentForm.hasHtmlPayButton());
        webPaymentForm.setHtmlPayButtonText(payButton.getText());
        webPaymentForm.setHtmlHeaderText("Please enter your payment information");
        Region paymentRegion = webPaymentForm.buildEmbeddedPaymentForm();
        if (paymentRegion == null) { // This indicates a redirected payment form
            // Temporary UI (just spinner)
            paymentRegion = Controls.createPageSizeSpinner();
        }

        ScalePane scaledGatewayLogo = new ScalePane(new MonoPane(gatewayLogo));
        scaledGatewayLogo.setStretchWidth(true);

        mainVbox = new VBox(10,
            scaledGatewayLogo,
            paymentRegion
        );

        if (webPaymentForm.isSandbox()) {
            mainVbox.getChildren().add(webPaymentForm.createSandboxBar());
        }

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
                .inUiThread()
                .onComplete(ar -> {
                    if (cancelPaymentResultHandler != null) {
                        cancelPaymentResultHandler.accept(ar);
                    } else if (ar.failed())
                        errorConsumer.accept(ar.cause().getMessage());
                    else {
                        cancelConsumer.accept(ar.result());
                    }
                });
        });
        payButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        FlexPane buttonBar = new FlexPane(cancelButton, payButton);
        buttonBar.setHorizontalSpace(10);
        VBox.setMargin(buttonBar, new Insets(10, 0, 10, 0));
        mainVbox.getChildren().add(buttonBar);

        webPaymentForm
            .setOnLoadFailure(errorMsg -> {
                errorConsumer.accept(BookI18nKeys.ErrorWhileLoadingPaymentForm);
                Console.log(errorMsg);
            })
            .setOnInitFailure(errorMsg -> {
                errorConsumer.accept(BookI18nKeys.ErrorWhileInitializingHTMLPaymentForm);
                Console.log(errorMsg);
            })
            .setOnVerificationFailure(errorMsg -> {
                errorConsumer.accept(BookI18nKeys.ErrorPaymentGatewayFailure);
                Console.log(errorMsg);
            })
            .setOnPaymentFailure(errorMsg -> {
                errorConsumer.accept(BookI18nKeys.ErrorPaymentModalityFailure);
                Console.log(errorMsg);
            })
            .setOnPaymentCompletion(result -> {
                successConsumer.accept(result.paymentStatus());
            });
    }

    @Override
    public String getGatewayName() {
        return gatewayName;
    }

    @Override
    public void setCancelPaymentResultHandler(Consumer<AsyncResult<CancelPaymentResult>> cancelPaymentResultHandler) {
        this.cancelPaymentResultHandler = cancelPaymentResultHandler;
    }

    @Override
    public VBox getView() {
        return mainVbox;
    }

    //@Override
    void turnOnWaitMode() {
        if (pressedButton == payButton)
            turnOnButtonWaitMode(payButton, cancelButton);
        else
            turnOnButtonWaitMode(cancelButton, payButton);
    }

    //@Override
    void turnOffWaitMode() {
        turnOffButtonWaitMode(payButton, EcommerceI18nKeys.Pay);
        turnOffButtonWaitMode(cancelButton, BaseI18nKeys.Cancel);
    }

}
