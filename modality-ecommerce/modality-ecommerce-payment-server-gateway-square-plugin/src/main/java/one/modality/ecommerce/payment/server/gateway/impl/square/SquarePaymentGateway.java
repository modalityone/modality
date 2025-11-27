package one.modality.ecommerce.payment.server.gateway.impl.square;

import com.squareup.square.AsyncSquareClient;
import com.squareup.square.core.Environment;
import com.squareup.square.core.SquareApiException;
import com.squareup.square.types.CreatePaymentRequest;
import com.squareup.square.types.CreatePaymentResponse;
import com.squareup.square.types.Currency;
import com.squareup.square.types.Error;
import com.squareup.square.types.Money;
import com.squareup.square.types.Payment;
import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.util.uuid.Uuid;
import one.modality.ecommerce.payment.PaymentStatus;
import one.modality.ecommerce.payment.SandboxCard;
import one.modality.ecommerce.payment.server.gateway.*;
import one.modality.ecommerce.payment.server.gateway.impl.util.RestApiOneTimeHtmlResponsesCache;

import static one.modality.ecommerce.payment.server.gateway.impl.square.SquareRestApiJob.SQUARE_PAYMENT_FORM_ENDPOINT;

/**
 * @author Bruno Salmon
 */
public final class SquarePaymentGateway implements PaymentGateway {

    private static final boolean DEBUG_LOG = true;

    static final String GATEWAY_NAME = "Square";

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
        // Reading the account parameters that have been loaded from the database by ServerPaymentServiceProvider
        String appId = argument.getAccountParameter("app_id");
        String locationId = argument.getAccountParameter("location_id"); // KBS3
        if (locationId == null)
            locationId = argument.getAccountParameter("order.order.location_id"); // KBS2 (to remove later)
        boolean live = argument.isLive();
        // Our Square gateway script implementation supports seamless integration.
        boolean seamless = argument.isSeamlessIfSupported()
            // && argument.isParentPageHttps() // Maybe would be better to not use seamless integration on http, but commented for now as iFrame integration is not working well in browser (ex: WebPaymentForm fitHeight not working well)
            ;
        String template = seamless ? SCRIPT_TEMPLATE : HTML_TEMPLATE;
        String paymentFormContent = template
            .replace("${modality_amount}", Long.toString(argument.getAmount()))
            .replace("${modality_currencyCode}", argument.getCurrencyCode())
            .replace("${modality_seamless}", String.valueOf(seamless))
            .replace("${square_webPaymentsSDKUrl}", live ? SQUARE_LIVE_WEB_PAYMENTS_SDK_URL : SQUARE_SANDBOX_WEB_PAYMENTS_SDK_URL)
            .replace("${square_appId}", appId)
            .replace("${square_locationId}", locationId);
        if (DEBUG_LOG) {
            Console.log("[Square][DEBUG] initiatePayment - content = " + paymentFormContent);
        }
        SandboxCard[] sandboxCards = live ? null : SANDBOX_CARDS;
        if (seamless) {
            return Future.succeededFuture(GatewayInitiatePaymentResult.createEmbeddedContentInitiatePaymentResult(live, true, paymentFormContent, false, sandboxCards));
        } else { // In other cases, we embed the page in a WebView/iFrame that can be loaded through https (assuming this server is on https)
            String htmlCacheKey = Uuid.randomUuid();
            RestApiOneTimeHtmlResponsesCache.registerOneTimeHtmlResponse(htmlCacheKey, paymentFormContent);
            String url = SQUARE_PAYMENT_FORM_ENDPOINT.replace(":htmlCacheKey", htmlCacheKey);
            return Future.succeededFuture(GatewayInitiatePaymentResult.createEmbeddedUrlInitiatePaymentResult(live, false, url, false, sandboxCards));
        }
    }

    @Override
    public Future<GatewayCompletePaymentResult> completePayment(GatewayCompletePaymentArgument argument) {
        Promise<GatewayCompletePaymentResult> promise = Promise.promise();
        boolean live = argument.isLive();
        String accessToken = argument.getAccessToken();
        AsyncSquareClient client = AsyncSquareClient.builder()
            .environment(live ? Environment.PRODUCTION : Environment.SANDBOX)
            .token(accessToken)
            .build();
        ReadOnlyAstObject payload = AST.parseObject(argument.getPayload(), "json");
        Long amount = payload.getLong("modality_amount");
        String currencyCode = payload.getString("modality_currencyCode");
        String locationId = payload.getString("square_locationId");
        String idempotencyKey = payload.getString("square_idempotencyKey");
        String sourceId = payload.getString("square_sourceId");
        String verificationToken = payload.getString("square_verificationToken");
        if (DEBUG_LOG) {
            Console.log("[Square][DEBUG] completePayment - payload = " + argument.getPayload() + ", amount = " + amount + ", currencyCode = " + currencyCode + ", locationId = " + locationId + ", idempotencyKey = " + idempotencyKey + ", sourceId = " + sourceId + ", verificationToken = " + verificationToken);
        }

        // Use Square SDK async client pattern (like AsyncCustomersClient)
        client.payments()
            .create(
                CreatePaymentRequest.builder()
                    .sourceId(sourceId)
                    .idempotencyKey(idempotencyKey)
                    .locationId(locationId)
                    .verificationToken(verificationToken)
                    .amountMoney(Money.builder()
                        .amount(amount)
                        .currency(Currency.valueOf(currencyCode))
                        .build())
                    .build()
            )
            .thenAccept(response -> {
                // Extract payment from response (it's an Optional in v45)
                Payment payment = response.getPayment().orElse(null);
                if (payment != null) {
                    // We generate the final result from the payment information and also capture the response (stored in the database)
                    promise.complete(generateResultFromSquarePayment(payment, response));
                } else {
                    promise.fail("[Square] completePayment - Payment is null in response");
                }
            })
            .exceptionally(ex -> {
                if (DEBUG_LOG) {
                    Console.log("[Square][DEBUG] completePayment - exception:", ex);
                }
                // Extract the cause if wrapped
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                String message = "[Square] completePayment - Square raised exception " + cause.getMessage();

                // Enhanced logging for better debugging
                if (cause instanceof SquareApiException ae) {
                    message += " | Errors: ";
                    for (Error error : ae.errors()) {
                        message += error.getCategory() + ": " + error.getCode() + " - " + error.getDetail() + "; ";
                    }
                    Console.log(message);
                } else {
                    Console.log(message);
                }
                promise.fail(message);
                return null;  // Required for exceptionally
            });
        return promise.future();
    }

    private static GatewayCompletePaymentResult generateResultFromSquarePayment(Payment payment, CreatePaymentResponse response) {
        // In v45, we serialize the response object to JSON for storage
        String gatewayResponse = response.toString(); // This will be stored in the database
        String gatewayTransactionRef = payment.getId().orElse(null);
        String gatewayStatus = payment.getStatus().orElse("UNKNOWN");
        SquarePaymentStatus squarePaymentStatus = SquarePaymentStatus.valueOf(gatewayStatus.toUpperCase());
        PaymentStatus paymentStatus = squarePaymentStatus.getGenericPaymentStatus();
        return new GatewayCompletePaymentResult(gatewayResponse, gatewayTransactionRef, gatewayStatus, paymentStatus);
    }
}
