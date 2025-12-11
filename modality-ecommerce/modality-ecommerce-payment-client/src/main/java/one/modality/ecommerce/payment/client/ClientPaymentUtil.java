package one.modality.ecommerce.payment.client;

import dev.webfx.extras.webview.pane.WebViewPane;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.origin.client.ClientOrigin;
import one.modality.ecommerce.payment.PaymentAllocation;
import one.modality.ecommerce.payment.InitiatePaymentArgument;
import one.modality.ecommerce.payment.PaymentFormType;

/**
 * @author Bruno Salmon
 */
public final class ClientPaymentUtil {

    public static InitiatePaymentArgument createInitiatePaymentArgumentForSingleDocumentPayment(int amount, Object documentPrimaryKey, PaymentFormType preferredFormType, String returnRoute, String cancelRoute) {
        return createInitiatePaymentArgument(amount, new PaymentAllocation[] { new PaymentAllocation(documentPrimaryKey, amount) }, preferredFormType, returnRoute, cancelRoute);
    }

    public static InitiatePaymentArgument createInitiatePaymentArgument(int amount, PaymentAllocation[] paymentAllocations, PaymentFormType preferredFormType, String returnRoute, String cancelRoute) {
        return new InitiatePaymentArgument(
            amount,
            paymentAllocations,
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
        return ClientOrigin.getClientAppRouteUrl(route);
    }

}
