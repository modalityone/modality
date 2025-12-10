package one.modality.ecommerce.payment.server.gateway.impl.square;

import com.squareup.square.AsyncSquareClient;
import com.squareup.square.core.Environment;
import com.squareup.square.types.GetOrdersRequest;
import com.squareup.square.types.Order;
import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.http.HttpResponseStatus;
import dev.webfx.platform.util.vertx.VertxInstance;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.SystemUserId;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import one.modality.base.shared.entities.GatewayParameter;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.PaymentStatus;
import one.modality.ecommerce.payment.UpdatePaymentStatusArgument;
import one.modality.ecommerce.payment.server.gateway.impl.util.RestApiOneTimeHtmlResponsesCache;

/**
 * @author Bruno Salmon
 */
public final class SquareRestApiJob implements ApplicationJob {

    static final String SQUARE_PAYMENT_FORM_ENDPOINT = "/payment/square/paymentForm/:htmlCacheKey";
    // Scroll down to the second section for the webhook REST API constants

    @Override
    public void onInit() {
        Router router = VertxInstance.getHttpRouter();

        /*====================================== EMBED PAYMENT FORM REST API =========================================*/

        // This endpoint is called by the Modality front-office when the web payment form can't be initialized through
        // some direct HTML content, but requires this HTML content to be loaded through a later server REST call.
        // Typically, when it is embedded in an iFrame, iFrame.src is set to that endpoint to load the HTML code and
        // start the web payment form.
        router.route(SQUARE_PAYMENT_FORM_ENDPOINT)
            .handler(ctx -> {
                // Because it is a later call just after SquarePaymentGateway.initiatePayment(), we expect the
                // content to be present in the HTML cache, as set by initiatePayment() just before.
                String cacheKey = ctx.pathParam("htmlCacheKey");
                String html = RestApiOneTimeHtmlResponsesCache.getOneTimeHtmlResponse(cacheKey);
                // And we return that content
                ctx.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML)
                    .end(html);
            });

        /*=========================================== WEBHOOKS REST API ==============================================*/

        // Endpoint for live payments Square web hook
        router.route(SQUARE_LIVE_WEBHOOK_ENDPOINT)
            .handler(BodyHandler.create()) // To ensure that the whole payload is loaded before calling the next handler
            .handler(ctx -> handleWebhook(ctx, true));

        // Same endpoint but for sandbox payments
        router.route(SQUARE_SANDBOX_WEBHOOK_ENDPOINT)
            .handler(BodyHandler.create()) // To ensure that the whole payload is loaded before calling the next handler
            .handler(ctx -> handleWebhook(ctx, false));
    }


    private static final String SQUARE_LIVE_WEBHOOK_ENDPOINT = "/payment/square/live/webhook";
    private static final String SQUARE_SANDBOX_WEBHOOK_ENDPOINT = "/payment/square/sandbox/webhook";

    private static final SystemUserId SQUARE_HISTORY_USER_ID = new SystemUserId(SquarePaymentGateway.GATEWAY_NAME);

    private void handleWebhook(RoutingContext ctx, boolean live) {
        JsonObject vertxPayload = ctx.body().asJsonObject();
        String logPrefix = "[Square] " + (live ? "Live" : "Sandbox") + " webhook - ";
        String textPayload = vertxPayload.encode();
        Console.log(logPrefix + "Called with payload = " + textPayload);

        AstObject payload = AST.createObject(vertxPayload);
        String squareEventType = AST.lookupString(payload, "type");
        if (!"payment.updated".equals(squareEventType)) {
            Console.log(logPrefix + "⚠️  Received an event type that is not managed by this webhook: " + squareEventType);
        } else {
            // If the payment was created through the embedded payment form, the Square payment id has been recorded
            // as transactionRef by `ServerPaymentServiceProvider.completePayment()`. So we can find it by this id.
            String transactionRef = AST.lookupString(payload, "data.object.payment.id");
            if (transactionRef == null)
                Console.log(logPrefix + "⛔️  Couldn't find data.object.payment.id in the payload!");
            else
                loadAndUpdatePaymentStatus("transactionRef", transactionRef, live, payload, textPayload, logPrefix);
        }
        ctx.response().setStatusCode(HttpResponseStatus.OK_200).end();
    }

    private static void loadAndUpdatePaymentStatus(String field, String value, boolean live, AstObject payload, String textPayload, String logPrefix) {
        EntityStore.create()
            .<MoneyTransfer>executeQuery("select pending,successful,status,gatewayResponse from MoneyTransfer where " + field + " = ?", value)
            .onFailure(e -> Console.log(logPrefix + "⛔️️  An error occurred when reading the payment with " + field + " = " + value, e))
            .onSuccess(payments -> {
                int n = payments.size();
                if (n == 1)
                    updatePaymentStatus(payments.get(0), payload, textPayload, logPrefix);
                else if (n != 0 || "id".equals(field)) {
                    Console.log(logPrefix + "⛔️️  " + payments.size() + " payments were found in the database with " + field + " = " + value + " !");
                } else {
                    // We didn't find it, but this can happen with redirected payments because completePayment()
                    // has not been called for them. The way to find a redirected payment is to retrieve its
                    // order in the Square API and then read its referenceId (this is where the payment id is stored).
                    // Because we need to access the Square API, we first need to get the Square access token from the
                    // Modality account parameters.
                    String applicationId = AST.lookupString(payload, "data.object.payment.application_details.application_id");
                    String orderId = AST.lookupString(payload, "data.object.payment.order_id");
                    if (applicationId == null)
                        Console.log(logPrefix + "⛔️️  No application_id was found in the payload!");
                    else if (orderId == null)
                        Console.log(logPrefix + "⛔️️  No order_id was found in the payload!");
                    else {
                        payments.getStore().<GatewayParameter>executeQuery("select value from AccountParameter p where account.gatewayCompany.name='Square' and name='access_token' and live=$1 and exists(select AccountParameter where account=p.account and live=$1 and name='app_id' and value=$2) order by id desc limit 1", live, applicationId)
                            .onFailure(e -> Console.log(logPrefix + "⛔️️  An error occurred when reading the account parameters", e))
                            .onSuccess(parameters -> {
                                GatewayParameter accessTokenParameter = Collections.first(parameters);
                                if (accessTokenParameter == null)
                                    Console.log(logPrefix + "⛔️️  No access token was found in the database for the Square account with applicationId = " + applicationId + " and live = " + live);
                                else {
                                    String accessToken = accessTokenParameter.getValue();
                                    AsyncSquareClient client = AsyncSquareClient.builder()
                                        .environment(live ? Environment.PRODUCTION : Environment.SANDBOX)
                                        .token(accessToken)
                                        .build();
                                    client.orders().get(GetOrdersRequest.builder()
                                        .orderId(orderId)
                                        .build()
                                    ).thenAccept(response -> {
                                        Order order = response.getOrder().orElse(null);
                                        if (order == null)
                                            Console.log(logPrefix + "⛔️️  No order was found in the Square API for orderId = " + orderId);
                                        else {
                                            String referenceId = order.getReferenceId().orElse(null);
                                            if (referenceId == null)
                                                Console.log(logPrefix + "⛔️️  No referenceId was found in the Square API for orderId = " + orderId);
                                            else
                                                loadAndUpdatePaymentStatus("id", referenceId, live, payload, textPayload, logPrefix);
                                        }
                                    });
                                }
                            });
                    }
                }
            });
    }

    private static void updatePaymentStatus(MoneyTransfer payment, AstObject payload, String textPayload, String logPrefix) {
        ReadOnlyAstObject paymentObject = AST.lookupObject(payload, "data.object.payment");
        String id = paymentObject.getString("id"); // Corresponds to transactionRef in Modality
        String status = paymentObject.getString("status");
        SquarePaymentStatus squarePaymentStatus = SquarePaymentStatus.valueOf(status);
        PaymentStatus paymentStatus = squarePaymentStatus.getGenericPaymentStatus();
        boolean pending = paymentStatus.isPending();
        boolean successful = paymentStatus.isSuccessful();
        Object paymentPk = payment.getPrimaryKey();
        // Maybe this Square event doesn't really change the payment status (Square sometimes
        // sends very similar events with the same status, only change is in the payload)
        if (payment.isPending() == pending && payment.isSuccessful() == successful && status.equals(payment.getStatus())) {
            // In that case, we only update the payload in the database (if the version is newer)
            ReadOnlyAstObject dbPayload = AST.parseObject(payment.getGatewayResponse(), "json");
            ReadOnlyAstObject dbPaymentObject = AST.lookupObject(dbPayload, "data.object.payment");
            if (dbPaymentObject != null && dbPaymentObject.getInteger("version") >= paymentObject.getInteger("version")) {
                Console.log(logPrefix + "Skipping this event because it's not newer compared to the database");
            } else {
                // We do a simple update of the payment gateway response without creating a new history entry
                UpdateStore updateStore = UpdateStore.createAbove(payment.getStore());
                payment = updateStore.updateEntity(payment);
                payment.setGatewayResponse(textPayload);
                updateStore.submitChanges()
                    .onFailure(e -> Console.log(logPrefix + "⛔️️  Failed to update gatewayResponse for payment " + paymentPk, e))
                    .onSuccess(v -> Console.log(logPrefix + "✅  Successfully updated gatewayResponse for payment " + paymentPk));
            }
        } else {
            // We finally update the payment status through the payment service (this will also create a history entry)
            SQUARE_HISTORY_USER_ID.callAndReturn(() ->
                PaymentService.updatePaymentStatus(UpdatePaymentStatusArgument.createCapturedStatusArgument(paymentPk.toString(), textPayload, id, status, pending, successful))
                    .onFailure(e -> Console.log(logPrefix + "⛔️️  Failed to update status " + status + " for transactionRef = " + id, e))
                    .onSuccess(v -> Console.log(logPrefix + "✅  Successfully updated status " + status + " for transactionRef = " + id))
            );
        }
    }

}