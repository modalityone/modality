package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.payment.InitiatePaymentArgument;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.event.fx.FXEvent;

public class Step4PaymentSlide extends StepSlide {

    private final HtmlText paymentInformationInPaymentSlide = new HtmlText();
    private Label bookedEventTitleText;
    private final HtmlText messageHtmlText = new HtmlText();

    public Step4PaymentSlide(SlideController control, BookEventData bed) {
        super(control, bed);
        controller.setStep4PaymentSlide(this);
        I18n.bindI18nTextProperty(paymentInformationInPaymentSlide.textProperty(), "PaymentInformation");
    }

    public void setWebPaymentForm(WebPaymentForm webPaymentForm) {
        Region paymentRegion = webPaymentForm.buildPaymentForm();
        mainVbox.getChildren().add(paymentRegion);
        Button payButton = I18nControls.bindI18nProperties(new Button(), "Pay");
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (webPaymentForm.isUserInteractionAllowed()) {
                turnOffButtonWaitMode(payButton, "Pay");
            } else {
                turnOnButtonWaitMode(payButton);
            }
        }, webPaymentForm.userInteractionAllowedProperty());
        Button cancelButton = I18nControls.bindI18nProperties(new Button(), "Cancel");
        payButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        payButton.setOnAction(e -> webPaymentForm.pay());
        cancelButton.setOnAction(e -> controller.displayErrorMessage("ErrorUserCanceledPayment"));
        FlexPane buttonBar = new FlexPane(payButton, cancelButton);
        buttonBar.setHorizontalSpace(10);
        VBox.setMargin(buttonBar, new Insets(10, 0, 10, 0));
        mainVbox.getChildren().add(buttonBar);
        //paymentRegion.
        bookedEventTitleText.setText(FXEvent.getEvent().getName() +  " | Total booking price: " + bookEventData.getPriceCalculator().calculateTotalPrice(bookEventData.getCurrentBooking().getLastestDocumentAggregate())/100 + "£");
        webPaymentForm
                .setOnLoadFailure(errorMsg -> {
                    controller.displayErrorMessage("ErrorWhileLoadingPaymentForm");
                    Console.log(errorMsg);
                })
                .setOnInitFailure(errorMsg -> {
                    controller.displayErrorMessage("ErrorWhileInitializingHTMLPaymentForm");
                    Console.log(errorMsg);
                })
                .setOnGatewayFailure(errorMsg -> {
                    controller.displayErrorMessage("ErrorPaymentGatewayFailure");
                    Console.log(errorMsg);
                })
                .setOnModalityFailure(errorMsg -> {
                    controller.displayErrorMessage("ErrorPaymentModalityFailure");
                    Console.log(errorMsg);
                })
                .setOnFinalStatus(status -> {
                    //TODO: to test the following
                    if(status.isSuccessful()) {
                        messageHtmlText.setText("");
                        controller.displayNextSlide();
                    }
                    if(status.isFailed()) {
                        mainVbox.getChildren().remove(paymentRegion);
                        messageHtmlText.setText(I18n.getI18nText("PaymentFailed"));
                        Button retryPayButton = I18nControls.bindI18nProperties(new Button(), "RetryPayment");
                        //We manage the property of the button in css
                        retryPayButton.setGraphicTextGap(30);
                        retryPayButton.getStyleClass().addAll("event-button", "success-button");
                        retryPayButton.setMaxWidth(150);
                        mainVbox.getChildren().add(retryPayButton);
                        retryPayButton.setOnAction(event -> {
                            turnOnButtonWaitMode(retryPayButton);
                            PaymentService.initiatePayment(
                                            new InitiatePaymentArgument(bookEventData.getTotalPrice(), bookEventData.getDocumentPrimaryKey())
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

    public void buildUi() {
        mainVbox.getChildren().clear();
        mainVbox.setAlignment(Pos.TOP_CENTER);
        bookedEventTitleText = new Label();
        bookedEventTitleText.getStyleClass().addAll("book-event-primary-title", "emphasize");
        HBox line1 = new HBox(bookedEventTitleText);
        line1.setAlignment(Pos.CENTER_LEFT);
        //bookedEventTitleText.setPrefWidth(MAX_WIDTH - 50);
        line1.setPadding(new Insets(20, 0, 0, 50));
        mainVbox.getChildren().add(line1);
        paymentInformationInPaymentSlide.getStyleClass().add("subtitle-grey");
        paymentInformationInPaymentSlide.setMaxWidth(600);
        paymentInformationInPaymentSlide.setPadding(new Insets(50,0,20,0));
        HBox line2 = new HBox(paymentInformationInPaymentSlide);
        line2.setAlignment(Pos.BASELINE_LEFT);
        line2.setPadding(new Insets(10, 0, 20, 50));
        mainVbox.getChildren().add(line2);
    }
}
