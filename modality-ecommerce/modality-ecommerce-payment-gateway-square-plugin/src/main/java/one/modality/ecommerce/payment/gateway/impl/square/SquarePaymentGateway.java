package one.modality.ecommerce.payment.gateway.impl.square;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.util.uuid.Uuid;
import one.modality.ecommerce.payment.gateway.*;

/**
 * @author Bruno Salmon
 */
public final class SquarePaymentGateway implements PaymentGateway {

    private static final String GATEWAY_NAME = "Square";

    private static final String HTML_TEMPLATE = Resource.getText(Resource.toUrl("square-checkout.html", SquarePaymentGateway.class));

    @Override
    public String getName() {
        return GATEWAY_NAME;
    }

    @Override
    public Future<GatewayInitiatePaymentResult> initiatePayment(GatewayInitiatePaymentArgument argument) {
        String appId = argument.getAccountParameter("app_id");
        String locationId = argument.getAccountParameter("order.order.location_id");
        String html = HTML_TEMPLATE.replace("${appId}", appId).replace("${locationId}", locationId);
        String responseCacheKey = Uuid.randomUuid();
        SquareRestApiOneTimeHtmlResponsesCache.registerOneTimeHtmlResponse(responseCacheKey, html);
        String url = SquareRestApiStarterJob.SQUARE_EMBEDDED_PAYMENT_URL.replace(":responseCacheKey", responseCacheKey);
        return Future.succeededFuture(new GatewayInitiatePaymentResult(url, false));
    }

    @Override
    public Future<GatewayMakeApiPaymentResult> makeApiPayment(GatewayMakeApiPaymentArgument argument) {
        return Future.failedFuture("makeApiPayment() not yet implemented for Square");
    }
}
