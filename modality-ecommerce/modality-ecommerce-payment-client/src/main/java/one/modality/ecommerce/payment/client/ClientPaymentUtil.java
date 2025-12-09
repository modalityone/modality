package one.modality.ecommerce.payment.client;

import dev.webfx.extras.webview.pane.WebViewPane;
import dev.webfx.platform.windowlocation.WindowLocation;
import one.modality.ecommerce.payment.InitiatePaymentArgument;
import one.modality.ecommerce.payment.PaymentFormType;

/**
 * @author Bruno Salmon
 */
public final class ClientPaymentUtil {

    public static InitiatePaymentArgument createInitiatePaymentArgument(int amount, Object documentPrimaryKey, PaymentFormType preferredFormType, String returnRoute, String cancelRoute) {
        return new InitiatePaymentArgument(
            amount,
            documentPrimaryKey,
            preferredFormType,
            WebViewPane.isBrowser(),
            "https".equalsIgnoreCase(WindowLocation.getProtocol()),
            routeToUrl(returnRoute),
            routeToUrl(cancelRoute)
        );
    }

    private static String routeToUrl(String route) {
        if (route == null)
            return null;
        return WindowLocation.getOrigin() + "/#" + route;
    }

}
