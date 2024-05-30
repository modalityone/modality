package one.modality.ecommerce.payment.embedded.ui;

import dev.webfx.platform.console.Console;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import one.modality.ecommerce.payment.embedded.EmbeddedPaymentService;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentArgument;

/**
 * @author Bruno Salmon
 */
public class EmbeddedPaymentUI {

    private final InitiateEmbeddedPaymentArgument testPaymentArg =
            new InitiateEmbeddedPaymentArgument(12500, "USD", "Amazing event", 1, null, null, null);

    public Node buildNode() {
        WebView webView = new WebView();
        Button checkoutButton = new Button("Checkout");
        checkoutButton.setOnAction(e -> EmbeddedPaymentService.initiateEmbeddedPayment(testPaymentArg)
                .onFailure(Console::log)
                .onSuccess(r -> Platform.runLater(() -> webView.getEngine().loadContent(r.getHtmlContent()))));
        BorderPane borderPane = new BorderPane(webView);
        borderPane.setTop(checkoutButton);
        return borderPane;
    }
}
