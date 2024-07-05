package one.modality.ecommerce.payment.server.gateway.impl.square;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.util.uuid.Uuid;
import one.modality.ecommerce.payment.SandboxCard;
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

    private static final SandboxCard[] SANDBOX_CARDS = {
            new SandboxCard("Visa - Success", "4111 1111 1111 1111", null, "111", "11111"),
            new SandboxCard("Mastercard - Success", "5105 1051 0510 5100", null, "111", "11111"),
            new SandboxCard("Discover - Success", "6011 0000 0000 0004", null, "111", "11111"),
            new SandboxCard("JCB - Success", "3569 9900 1009 5841", null, "111", null),
            new SandboxCard("American Express - Success", "6011 0000 0000 0004", null, "1111", "11111"),
            new SandboxCard("China Union Pay - Success", "6222 9888 1234 0000", null, "111", null),
            new SandboxCard("Square Gift Card - Success", "6011 0000 0000 0004", null, "111", "11111"),
            new SandboxCard("CVV incorrect", null, null, "911", null),
            new SandboxCard("Postal code incorrect", null, null, null, "99999"),
            new SandboxCard("Expiration date incorrect", null, "01/40", null, null),
            new SandboxCard("Declined number", "4000000000000002", null, null, null),
            new SandboxCard("On file auth declined", "4000000000000010", null, null, null),
            new SandboxCard("Visa - No challenge", "4800 0000 0000 0004", null, "111", "11111"),
            new SandboxCard("Mastercard - No challenge", "5222 2200 0000 0005", null, "111", "11111"),
            new SandboxCard("Discover EU - No challenge", "6011 0000 0020 1016", null, "111", "11111"),
            new SandboxCard("JCB - Success", "3569 9900 1009 5841", null, "111", null),
            new SandboxCard("Visa EU - Verification code: 123456", "4310 0000 0020 1019", null, "1111", "11111"),
            new SandboxCard("Mastercard - Verification code: 123456", "5248 4800 0021 0026", null, "1111", "11111"),
            new SandboxCard("Mastercard EU - Verification code: 123456", "5500 0000 0020 1016", null, "1111", "11111"),
            new SandboxCard("American Express EU - Verification code: 123456", "3700 000002 01014", null, "1111", "11111"),
            new SandboxCard("Visa - Failed verification", "4811 1100 0000 0008", null, "1111", "11111")
    };

    @Override
    public String getName() {
        return GATEWAY_NAME;
    }

    @Override
    public Future<GatewayInitiatePaymentResult> initiatePayment(GatewayInitiatePaymentArgument argument) {
        String appId = argument.getAccountParameter("app_id");
        String locationId = argument.getAccountParameter("order.order.location_id");
        boolean live = argument.isLive();
        // Our Square gateway script implementation supports seamless integration.
        boolean seamless = argument.isSeamlessIfSupported()
            // && argument.isParentPageHttps() // Maybe would be better to not use seamless integration on http, but commented for now as iFrame integration is not working well in browser (ex: WebPaymentForm fitHeight not working well)
        ;
        String template = seamless ? SCRIPT_TEMPLATE : HTML_TEMPLATE;
        template = template
                .replace("${modality_seamless}", String.valueOf(seamless))
                .replace("${square_webPaymentsSDKUrl}", live ? SQUARE_LIVE_WEB_PAYMENTS_SDK_URL : SQUARE_SANDBOX_WEB_PAYMENTS_SDK_URL)
                .replace("${square_appId}", appId)
                .replace("${square_locationId}", locationId)
                .replace("${modality_paymentId}", argument.getPaymentId())
                .replace("${modality_amount}", Long.toString(argument.getAmount()))
                .replace("${modality_currencyCode}", argument.getCurrencyCode())
                .replace("${modality_completePaymentRoute}", live ? SQUARE_LIVE_COMPLETE_PAYMENT_ENDPOINT : SQUARE_SANDBOX_COMPLETE_PAYMENT_ENDPOINT)
                ;
        SandboxCard[] sandboxCards = live ? null : SANDBOX_CARDS;
        if (seamless) {
            return Future.succeededFuture(GatewayInitiatePaymentResult.createEmbeddedContentInitiatePaymentResult(live, true, template, sandboxCards));
        } else { // In other cases, we embed the page in a WebView/iFrame that can be loaded through https (assuming this server is on https)
            String htmlCacheKey = Uuid.randomUuid();
            SquareRestApiOneTimeHtmlResponsesCache.registerOneTimeHtmlResponse(htmlCacheKey, template);
            String url = SQUARE_PAYMENT_FORM_ENDPOINT.replace(":htmlCacheKey", htmlCacheKey);
            return Future.succeededFuture(GatewayInitiatePaymentResult.createEmbeddedUrlInitiatePaymentResult(live, false, url, sandboxCards));
        }
    }

    @Override
    public Future<GatewayMakeApiPaymentResult> makeApiPayment(GatewayMakeApiPaymentArgument argument) {
        return Future.failedFuture("makeApiPayment() not yet implemented for Square");
    }
}
