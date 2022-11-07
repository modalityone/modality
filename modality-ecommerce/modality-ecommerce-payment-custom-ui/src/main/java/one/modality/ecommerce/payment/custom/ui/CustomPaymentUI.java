package one.modality.ecommerce.payment.custom.ui;

import dev.webfx.platform.console.Console;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import one.modality.ecommerce.payment.custom.CustomPaymentService;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentArgument;

/**
 * @author Bruno Salmon
 */
public class CustomPaymentUI {

    private final InitiateCustomPaymentArgument testPaymentArg =
            new InitiateCustomPaymentArgument(12500, "USD", "Amazing event", 1, null, null, null);

    public Node buildNode() {
        WebView webView = new WebView();
        Button checkoutButton = new Button("Checkout");
        checkoutButton.setOnAction(e -> CustomPaymentService.initiateCustomPayment(testPaymentArg)
                .onFailure(Console::log)
                .onSuccess(r -> Platform.runLater(() -> webView.getEngine().loadContent(r.getHtmlContent()))));
        BorderPane borderPane = new BorderPane(webView);
        borderPane.setTop(checkoutButton);
        return borderPane;
    }
}
