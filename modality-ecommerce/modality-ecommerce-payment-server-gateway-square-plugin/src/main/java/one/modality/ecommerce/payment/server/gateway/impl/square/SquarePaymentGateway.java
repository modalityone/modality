package one.modality.ecommerce.payment.server.gateway.impl.square;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.util.uuid.Uuid;
import one.modality.ecommerce.payment.server.gateway.*;

import static one.modality.ecommerce.payment.server.gateway.impl.square.SquareRestApiStarterJob.*;

/**
 * @author Bruno Salmon
 */
public final class SquarePaymentGateway implements PaymentGateway {

    private static final String GATEWAY_NAME = "Square";
    private static final String SQUARE_LIVE_WEB_PAYMENTS_SDK_URL = "https://web.squarecdn.com/v1/square.js";
    private static final String SQUARE_SANDBOX_WEB_PAYMENTS_SDK_URL = "https://sandbox.web.squarecdn.com/v1/square.js";

    private static final String CSS_TEMPLATE = Resource.getText(Resource.toUrl("square-payment-form.css", SquarePaymentGateway.class));
    private static final String SCRIPT_TEMPLATE = Resource.getText(Resource.toUrl("square-payment-form.js", SquarePaymentGateway.class));
    private static final String HTML_TEMPLATE = Resource.getText(Resource.toUrl("square-payment-form-iframe.html", SquarePaymentGateway.class))
            .replace("${square_paymentFormScript}", SCRIPT_TEMPLATE)
            .replace("${square_paymentFormCSS}", CSS_TEMPLATE);

    @Override
    public String getName() {
        return GATEWAY_NAME;
    }

    @Override
    public Future<GatewayInitiatePaymentResult> initiatePayment(GatewayInitiatePaymentArgument argument) {
        String appId = argument.getAccountParameter("app_id");
        String locationId = argument.getAccountParameter("order.order.location_id");
        boolean live = argument.isLive();
        boolean seamless = argument.isSeamlessIfSupported();
        String template = seamless ? SCRIPT_TEMPLATE : HTML_TEMPLATE;
        template = template
                .replace("${modality_seamless}", String.valueOf(seamless))
                .replace("${square_webPaymentsSDKUrl}", live ? SQUARE_LIVE_WEB_PAYMENTS_SDK_URL : SQUARE_SANDBOX_WEB_PAYMENTS_SDK_URL)
                .replace("${square_appId}", appId)
                .replace("${square_locationId}", locationId)
                .replace("${modality_paymentId}", argument.getPaymentId())
                .replace("${modality_amount}", Long.toString(argument.getAmount()))
                .replace("${modality_currencyCode}", argument.getCurrencyCode())
                .replace("${modality_completePaymentRoute}", live ? SQUARE_LIVE_COMPLETE_PAYMENT_ROUTE : SQUARE_SANDBOX_COMPLETE_PAYMENT_ROUTE)
                ;
        if (seamless) {
            return Future.succeededFuture(GatewayInitiatePaymentResult.createEmbeddedContentInitiatePaymentResult(live, true, template));
        } else {
            String htmlCacheKey = Uuid.randomUuid();
            SquareRestApiOneTimeHtmlResponsesCache.registerOneTimeHtmlResponse(htmlCacheKey, template);
            String url = SQUARE_PAYMENT_FORM_ROUTE.replace(":htmlCacheKey", htmlCacheKey);
            return Future.succeededFuture(GatewayInitiatePaymentResult.createEmbeddedUrlInitiatePaymentResult(live, false, url));
        }
    }

    @Override
    public Future<GatewayMakeApiPaymentResult> makeApiPayment(GatewayMakeApiPaymentArgument argument) {
        return Future.failedFuture("makeApiPayment() not yet implemented for Square");
    }
}
