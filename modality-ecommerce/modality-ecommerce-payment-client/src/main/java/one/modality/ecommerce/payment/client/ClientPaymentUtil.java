package one.modality.ecommerce.payment.client;

import dev.webfx.extras.webview.pane.WebViewPane;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.origin.client.ClientOrigin;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.payment.InitiatePaymentArgument;
import one.modality.ecommerce.payment.PaymentAllocation;
import one.modality.ecommerce.payment.PaymentFormType;
import one.modality.ecommerce.payment.PaymentService;

/**
 * @author Bruno Salmon
 */
public final class ClientPaymentUtil {

    public static Future<WebPaymentForm> initiateRedirectedPaymentAndRedirectToGatewayPaymentPage(int amount, PaymentAllocation[] paymentAllocations, PaymentFormType preferredFormType) {
        return PaymentService.initiatePayment(
                ClientPaymentUtil.createInitiatePaymentArgument(
                    amount,
                    paymentAllocations,
                    preferredFormType,
                    "/resume-payment/:moneyTransferId",
                    "/resume-payment/:moneyTransferId")
            )
            .inUiThread()
            .onFailure(Console::error)
            .map(paymentResult -> new WebPaymentForm(paymentResult, FXUserPerson.getUserPerson()));

    }

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
