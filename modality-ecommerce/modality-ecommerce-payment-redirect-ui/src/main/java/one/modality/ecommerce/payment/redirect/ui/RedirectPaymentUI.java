package one.modality.ecommerce.payment.redirect.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import one.modality.ecommerce.payment.redirect.RedirectPaymentService;
import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentArgument;

/**
 * @author Bruno Salmon
 */
public class RedirectPaymentUI {

    private final InitiateRedirectPaymentArgument testPaymentArg =
            new InitiateRedirectPaymentArgument("Amazing event", 12500, "USD");

    public Node buildNode() {
        WebView webView = new WebView();
        Button checkoutButton = new Button("Checkout");
        checkoutButton.setOnAction(e -> RedirectPaymentService.initiateRedirectPayment(testPaymentArg)
                .onSuccess(r -> Platform.runLater(() -> {
                    String paymentUrl = r.getRedirectPaymentUrl();
                    dev.webfx.platform.console.Console.log("Loading webView at " + paymentUrl);
                    webView.getEngine().load(paymentUrl);
                })));
        BorderPane borderPane = new BorderPane(webView);
        borderPane.setTop(checkoutButton);
        return borderPane;
    }
}
