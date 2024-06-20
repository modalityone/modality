package one.modality.ecommerce.payment.gateway.impl.square;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.util.uuid.Uuid;
import one.modality.ecommerce.payment.gateway.*;

import static one.modality.ecommerce.payment.gateway.impl.square.SquareRestApiStarterJob.*;

/**
 * @author Bruno Salmon
 */
public final class SquarePaymentGateway implements PaymentGateway {

    private static final String GATEWAY_NAME = "Square";
    private static final String SQUARE_LIVE_JS_LIBRARY_URL = "https://web.squarecdn.com/v1/square.js";
    private static final String SQUARE_SANDBOX_JS_LIBRARY_URL = "https://sandbox.web.squarecdn.com/v1/square.js";

    private static final String HTML_TEMPLATE = Resource.getText(Resource.toUrl("square-checkout.html", SquarePaymentGateway.class));

    @Override
    public String getName() {
        return GATEWAY_NAME;
    }

    @Override
    public Future<GatewayInitiatePaymentResult> initiatePayment(GatewayInitiatePaymentArgument argument) {
        String appId = argument.getAccountParameter("app_id");
        String locationId = argument.getAccountParameter("order.order.location_id");
        boolean live = argument.isLive();
        String html = HTML_TEMPLATE
                .replace("${squareJsLibraryUrl}", live ? SQUARE_LIVE_JS_LIBRARY_URL : SQUARE_SANDBOX_JS_LIBRARY_URL)
                .replace("${paymentId}", argument.getPaymentId())
                .replace("${amount}", Long.toString(argument.getAmount()))
                .replace("${currency}", argument.getCurrency())
                .replace("${appId}", appId)
                .replace("${locationId}", locationId)
                .replace("${completePaymentRoute}", live ? SQUARE_LIVE_COMPLETE_PAYMENT_ROUTE : SQUARE_SANDBOX_COMPLETE_PAYMENT_ROUTE)
                ;
        String responseCacheKey = Uuid.randomUuid();
        SquareRestApiOneTimeHtmlResponsesCache.registerOneTimeHtmlResponse(responseCacheKey, html);
        String url = SQUARE_PAYMENT_FORM_ROUTE.replace(":htmlCacheKey", responseCacheKey);
        return Future.succeededFuture(GatewayInitiatePaymentResult.createEmbeddedUrlInitiatePaymentResult(url));
    }

    @Override
    public Future<GatewayMakeApiPaymentResult> makeApiPayment(GatewayMakeApiPaymentArgument argument) {
        return Future.failedFuture("makeApiPayment() not yet implemented for Square");
    }
}
