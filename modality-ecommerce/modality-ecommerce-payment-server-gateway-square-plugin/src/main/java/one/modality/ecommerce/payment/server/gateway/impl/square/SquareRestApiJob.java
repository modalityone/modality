package one.modality.ecommerce.payment.server.gateway.impl.square;

import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.http.HttpResponseStatus;
import dev.webfx.platform.vertx.common.VertxInstance;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.SystemUserId;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.PaymentStatus;
import one.modality.ecommerce.payment.UpdatePaymentStatusArgument;

/**
 * @author Bruno Salmon
 */
public final class SquareRestApiJob implements ApplicationJob {

    static final String SQUARE_PAYMENT_FORM_ENDPOINT             = "/payment/square/paymentForm/:htmlCacheKey";
    private static final String SQUARE_LIVE_WEBHOOK_ENDPOINT     = "/payment/square/live/webhook";
    private static final String SQUARE_SANDBOX_WEBHOOK_ENDPOINT  = "/payment/square/sandbox/webhook";

    private static final SystemUserId SQUARE_HISTORY_USER_ID     = new SystemUserId("Square");

    @Override
    public void onInit() {
        Router router = VertxInstance.getHttpRouter();

        // This endpoint is called by the Modality front-office when the web payment form content requires a subsequent
        // server call, typically when it is embedded in an iFrame, iFrame.src is set to that endpoint to pull the html
        // code and start the web payment form.
        router.route(SQUARE_PAYMENT_FORM_ENDPOINT)
                .handler(ctx -> {
                    // Because it is a subsequent call to SquarePaymentGateway.initiatePayment(), we expect the content
                    // to be present in the html cache, as set by SquarePaymentGateway.initiatePayment() just a moment before
                    String cacheKey = ctx.pathParam("htmlCacheKey");
                    String html = SquareRestApiOneTimeHtmlResponsesCache.getOneTimeHtmlResponse(cacheKey);
                    // And we return that content
                    ctx.response()
                            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML)
                            .end(html);
                });

        // Endpoint for live payments Square web hook
        router.route(SQUARE_LIVE_WEBHOOK_ENDPOINT)
                .handler(BodyHandler.create()) // To ensure the whole payload is loaded before calling the next handler
                .handler(ctx -> handleWebhook(ctx, true));

        // Same endpoint but for sandbox payments
        router.route(SQUARE_SANDBOX_WEBHOOK_ENDPOINT)
                .handler(BodyHandler.create()) // To ensure the whole payload is loaded before calling the next handler
                .handler(ctx -> handleWebhook(ctx, false));
    }

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
            ReadOnlyAstObject paymentObject = AST.lookupObject(payload, "data.object.payment");
            if (paymentObject == null) {
                Console.log(logPrefix + "⛔️  Couldn't find the payment json object in the payload!");
            } else {
                String id = paymentObject.getString("id"); // Corresponds to transactionRef in Modality
                String status = paymentObject.getString("status");
                SquarePaymentStatus squarePaymentStatus = SquarePaymentStatus.valueOf(status);
                PaymentStatus paymentStatus = squarePaymentStatus.getGenericPaymentStatus();
                boolean pending = paymentStatus.isPending();
                boolean successful = paymentStatus.isSuccessful();
                EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                    .<MoneyTransfer>executeQuery("select pending, successful, status, gatewayResponse from MoneyTransfer where transactionRef=?", id)
                        .onFailure(e -> Console.log(logPrefix + "⛔️️  An error occurred when reading the payment with transactionRef = " + id, e))
                        .onSuccess(payments -> {
                            if (payments.isEmpty()) {
                                Console.log(logPrefix + "⛔️️  No payment was found in the database with transactionRef = " + id);
                            } else if (payments.size() != 1) {
                                Console.log(logPrefix + "⛔️️  " + payments.size() + " payments were found in the database with transactionRef = " + id);
                            } else { // We found one payment (no ambiguity)
                                MoneyTransfer payment = payments.get(0);
                                Object paymentPk = payment.getPrimaryKey();
                                // Maybe this Square event doesn't really change the payment status (Square often sends
                                // sometimes very similar events with the same status, only change is in the payload)
                                if (payment.isPending() == pending && payment.isSuccessful() == successful && status.equals(payment.getStatus())) {
                                    // In that case we only update the payload in the database (if the version is newer)
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
                        });
            }
        }
        ctx.response().setStatusCode(HttpResponseStatus.OK_200).end();
    }

}