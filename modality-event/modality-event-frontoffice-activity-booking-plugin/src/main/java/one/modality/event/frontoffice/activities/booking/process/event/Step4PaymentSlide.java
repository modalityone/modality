package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.client.ClientPaymentUtil;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.event.fx.FXEvent;

final class Step4PaymentSlide extends StepSlide {

    private final Label bookedEventTitleLabel = Bootstrap.textPrimary(Bootstrap.h4(new Label()));
    private final Label gatewayLogo = new Label();
    private final HtmlText paymentInformationHtmlText = Bootstrap.textPrimary(Bootstrap.h4(new HtmlText()));
    private final HtmlText messageHtmlText = new HtmlText();

    Step4PaymentSlide(SlideController control) {
        super(control);
    }

    void setWebPaymentForm(WebPaymentForm webPaymentForm) {
        BookEventData bookEventData = controller.getBookEventData();
        bookedEventTitleLabel.setWrapText(true);
        I18n.bindI18nTextProperty(paymentInformationHtmlText.textProperty(), "PaymentInformation", webPaymentForm.getGatewayName());
        I18nControls.bindI18nProperties(gatewayLogo, webPaymentForm.getGatewayName());
        Region paymentRegion = webPaymentForm.buildPaymentForm();
        mainVbox.getChildren().add(paymentRegion);
        if (webPaymentForm.isSandbox()) {
            mainVbox.getChildren().add(webPaymentForm.createSandboxBar());
        }
        Button payButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), "Pay", bookEventData.getFormattedBalanceProperty()));
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (webPaymentForm.isUserInteractionAllowed()) {
                turnOffButtonWaitMode(payButton, "Pay");
            } else {
                turnOnButtonWaitMode(payButton);
            }
        }, webPaymentForm.userInteractionAllowedProperty());
        Button cancelButton = Bootstrap.largeSecondaryButton(I18nControls.bindI18nProperties(new Button(), "Cancel"));
        payButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        payButton.setOnAction(e -> webPaymentForm.pay());
        cancelButton.setOnAction(e -> {
            turnOnButtonWaitMode(payButton);
            turnOnButtonWaitMode(cancelButton);
            webPaymentForm.cancelPayment()
                            .onComplete(ar -> UiScheduler.runInUiThread(() -> {
                                turnOffButtonWaitMode(payButton, "Pay");
                                turnOffButtonWaitMode(cancelButton, "Cancel");
                                controller.displayCancellationMessage();
                            }));
        });
        FlexPane buttonBar = new FlexPane(payButton, cancelButton);
        buttonBar.setHorizontalSpace(10);
        VBox.setMargin(buttonBar, new Insets(10, 0, 10, 0));
        mainVbox.getChildren().add(buttonBar);
        int totalPrice = bookEventData.getPriceCalculatorForCurrentOption().calculateTotalPrice();
        bookedEventTitleLabel.setText(FXEvent.getEvent().getName() + " |\u00A0Total\u00A0booking\u00A0price: " + bookEventData.getFormattedBalanceProperty().getValue());
        webPaymentForm
                .setOnLoadFailure(errorMsg -> {
                    controller.displayErrorMessage("ErrorWhileLoadingPaymentForm");
                    Console.log(errorMsg);
                })
                .setOnInitFailure(errorMsg -> {
                    controller.displayErrorMessage("ErrorWhileInitializingHTMLPaymentForm");
                    Console.log(errorMsg);
                })
                .setOnVerificationFailure(errorMsg -> {
                    controller.displayErrorMessage("ErrorPaymentGatewayFailure");
                    Console.log(errorMsg);
                })
                .setOnPaymentFailure(errorMsg -> {
                    controller.displayErrorMessage("ErrorPaymentModalityFailure");
                    Console.log(errorMsg);
                })
                .setOnPaymentCompletion(status -> {
                    //TODO: to test the following
                    if(status.isSuccessful()) {
                        messageHtmlText.setText("");
                        controller.displayNextSlide();
                    }
                    if(status.isFailed()) {
                        mainVbox.getChildren().remove(paymentRegion);
                        messageHtmlText.setText(I18n.getI18nText("PaymentFailed"));
                        Button retryPayButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), "RetryPayment"));
                        mainVbox.getChildren().add(retryPayButton);
                        retryPayButton.setOnAction(event -> {
                            turnOnButtonWaitMode(retryPayButton);
                            PaymentService.initiatePayment(
                                            ClientPaymentUtil.createInitiatePaymentArgument(totalPrice, bookEventData.getDocumentPrimaryKey())
                                    )
                                    .onFailure(paymentResult -> Platform.runLater(() -> {
                                        controller.displayErrorMessage("ErrorWhileInitiatingPayment");
                                        Console.log(paymentResult);
                                    }))
                                    .onSuccess(paymentResult -> Platform.runLater(() -> {
                                        WebPaymentForm newWebPaymentForm2 = new WebPaymentForm(paymentResult, FXUserPerson.getUserPerson());
                                        setWebPaymentForm(newWebPaymentForm2);
                                        mainVbox.getChildren().remove(retryPayButton);
                                    }));
                        });
                        if(status.isPending()) {
                            mainVbox.getChildren().remove(paymentRegion);
                            messageHtmlText.setText(I18n.getI18nText("PaymentPending"));
                        }
                    }
                });
    }

    void buildUi() {
        mainVbox.getChildren().clear();
        mainVbox.setAlignment(Pos.TOP_CENTER);
        MonoPane topPane = new MonoPane(bookedEventTitleLabel);
        topPane.setMaxWidth(Double.MAX_VALUE);
        topPane.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(topPane, new Insets(20,0,0,50));
        mainVbox.getChildren().add(topPane);

        paymentInformationHtmlText.getStyleClass().add("subtitle-grey");
        MonoPane paymentInfoPane = new MonoPane(paymentInformationHtmlText);
        paymentInfoPane.setMaxWidth(Double.MAX_VALUE);
        paymentInfoPane.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(paymentInfoPane, new Insets(10,0,20,50));
        mainVbox.getChildren().add(paymentInfoPane);

        MonoPane gatewayPane = new MonoPane(gatewayLogo);
        gatewayPane.setMaxWidth(Double.MAX_VALUE);
        gatewayPane.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(gatewayPane, new Insets(10,0,20,50));
        mainVbox.getChildren().add(gatewayPane);
    }
}
