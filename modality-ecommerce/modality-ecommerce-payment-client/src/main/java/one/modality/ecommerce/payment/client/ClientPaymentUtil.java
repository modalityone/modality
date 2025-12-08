package one.modality.ecommerce.payment.client;

import dev.webfx.extras.webview.pane.WebViewPane;
import dev.webfx.platform.windowlocation.WindowLocation;
import one.modality.ecommerce.payment.InitiatePaymentArgument;

/**
 * @author Bruno Salmon
 */
public final class ClientPaymentUtil {

    public static InitiatePaymentArgument createInitiatePaymentArgument(int amount, Object documentPrimaryKey) {
        return new InitiatePaymentArgument(
            amount,
            documentPrimaryKey,
            WebViewPane.isBrowser(),
            "https".equalsIgnoreCase(WindowLocation.getProtocol())
        );
    }

}
