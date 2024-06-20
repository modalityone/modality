package one.modality.ecommerce.payment.gateway.impl.square;

import com.squareup.square.Environment;
import com.squareup.square.SquareClient;
import com.squareup.square.api.PaymentsApi;
import com.squareup.square.authentication.BearerAuthModel;
import com.squareup.square.models.*;
import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.vertx.common.VertxInstance;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.UpdatePaymentStatusArgument;

/**
 * @author Bruno Salmon
 */
public final class SquareRestApiStarterJob implements ApplicationJob {

    static final String SQUARE_PAYMENT_FORM_ROUTE             = "/payment/square/paymentForm/:htmlCacheKey";
    static final String SQUARE_SANDBOX_COMPLETE_PAYMENT_ROUTE = "/payment/square/sandbox/completePayment";
    static final String SQUARE_LIVE_COMPLETE_PAYMENT_ROUTE    = "/payment/square/live/completePayment";

    @Override
    public void onInit() {
        Router router = VertxInstance.getHttpRouter();

        router.route(SQUARE_PAYMENT_FORM_ROUTE)
                .handler(ctx -> {
                    String cacheKey = ctx.pathParam("htmlCacheKey");
                    String html = SquareRestApiOneTimeHtmlResponsesCache.getOneTimeHtmlResponse(cacheKey);
                    ctx.response()
                            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML)
                            .end(html);
                });

        router.route(SQUARE_SANDBOX_COMPLETE_PAYMENT_ROUTE)
                .handler(ctx -> handleCompletePayment(ctx, false));

        router.route(SQUARE_LIVE_COMPLETE_PAYMENT_ROUTE)
                .handler(ctx -> handleCompletePayment(ctx, true));
    }

    private void handleCompletePayment(RoutingContext ctx, boolean live) {
        JsonObject payload = ctx.body().asJsonObject();
        String paymentId = payload.getString("paymentId");
        Long amount = payload.getLong("amount");
        String currency = payload.getString("currency");
        String locationId = payload.getString("locationId");
        String idempotencyKey = payload.getString("idempotencyKey");
        String sourceId = payload.getString("sourceId");
        String verificationToken = payload.getString("verificationToken");
        // TODO check all the above values are set, otherwise return an error

        PaymentService.loadPaymentGatewayParameters(paymentId, live)
                .onFailure(e -> ctx.end(e.getMessage()))
                .onSuccess(parameters -> {
                    String accessToken = parameters.get("access_token");
                    // TODO check accessToke is set, otherwise return an error
                    SquareClient client = new SquareClient.Builder()
                            .environment(live ? Environment.PRODUCTION : Environment.SANDBOX)
                            .bearerAuthCredentials(new BearerAuthModel.Builder(accessToken).build())
                            .build();
                    PaymentsApi paymentsApi = client.getPaymentsApi();
                    paymentsApi.createPaymentAsync(new CreatePaymentRequest.Builder(sourceId, idempotencyKey)
                            .locationId(locationId)
                            .verificationToken(verificationToken)
                            .amountMoney(new Money(amount, currency))
                            .build()
                    ).thenAccept(result -> {
                        Payment payment = result.getPayment();
                        JsonObject wholeResponseJson = new JsonObject();
                        wholeResponseJson.put("id", payment.getId());
                        wholeResponseJson.put("status", payment.getStatus());
                        wholeResponseJson.put("buyerEmailAddress", payment.getBuyerEmailAddress());
                        CardPaymentDetails cardDetails = payment.getCardDetails();
                        Card card = cardDetails == null ? null : cardDetails.getCard();
                        if (card != null) {
                            wholeResponseJson.put("cardBrand", card.getCardBrand());
                            wholeResponseJson.put("cardLast4", card.getLast4());
                        }
                        wholeResponseJson.put("orderId", payment.getOrderId());
                        wholeResponseJson.put("createdAt", payment.getCreatedAt());
                        wholeResponseJson.put("updatedAt", payment.getUpdatedAt());
                        wholeResponseJson.put("receiptNumber", payment.getReceiptNumber());
                        wholeResponseJson.put("receiptUrl", payment.getReceiptUrl());
                        String wholeResponse = wholeResponseJson.toString();
                        String status = payment.getStatus();
                        String transactionRef = payment.getId();
                        boolean successful = "completed".equalsIgnoreCase(status);
                        PaymentService.updatePaymentStatus(UpdatePaymentStatusArgument.createSuccessStatusArgument(paymentId, wholeResponse, transactionRef, status, successful))
                                .onSuccess(v -> ctx.end("OK"))
                                .onFailure(e -> ctx.end(e.getMessage()))
                        ;
                    }).exceptionally(ex -> {
                        PaymentService.updatePaymentStatus(UpdatePaymentStatusArgument.createErrorStatusArgument(paymentId, null, ex.getMessage()))
                                .onSuccess(v -> ctx.end(ex.getMessage()))
                                .onFailure(e -> ctx.end(e.getMessage()));
                        return null;
                    });
                });

    }
}
