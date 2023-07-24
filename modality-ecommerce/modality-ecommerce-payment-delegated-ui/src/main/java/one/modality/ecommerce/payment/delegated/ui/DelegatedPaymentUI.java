package one.modality.ecommerce.payment.delegated.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import one.modality.ecommerce.payment.delegated.DelegatedPaymentService;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentArgument;

/**
 * @author Bruno Salmon
 */
public class DelegatedPaymentUI {

  private final InitiateDelegatedPaymentArgument testPaymentArg =
      new InitiateDelegatedPaymentArgument("Amazing event", 12500, "USD");

  public Node buildNode() {
    WebView webView = new WebView();
    Button checkoutButton = new Button("Checkout");
    checkoutButton.setOnAction(
        e ->
            DelegatedPaymentService.initiateDelegatedPayment(testPaymentArg)
                .onSuccess(
                    r ->
                        Platform.runLater(
                            () -> {
                              String paymentUrl = r.getDelegatedPaymentUrl();
                              dev.webfx.platform.console.Console.log(
                                  "Loading webView at " + paymentUrl);
                              webView.getEngine().load(paymentUrl);
                            })));
    BorderPane borderPane = new BorderPane(webView);
    borderPane.setTop(checkoutButton);
    return borderPane;
  }
}
