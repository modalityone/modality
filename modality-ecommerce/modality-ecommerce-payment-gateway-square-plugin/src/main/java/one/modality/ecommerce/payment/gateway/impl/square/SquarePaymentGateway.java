package one.modality.ecommerce.payment.gateway.impl.square;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.vertx.common.VertxInstance;
import io.vertx.ext.web.Router;
import one.modality.ecommerce.payment.gateway.*;

/**
 * @author Bruno Salmon
 */
public class SquarePaymentGateway implements PaymentGateway {

    private static final String GATEWAY_NAME = "Square";
    private static final String SQUARE_EMBEDDED_PAYMENT_URL = "/payment/square/embedded-payment";

    public SquarePaymentGateway() {
        Router router = VertxInstance.getHttpRouter();
        router.route(SQUARE_EMBEDDED_PAYMENT_URL)
                .handler(ctx -> {
                    String html = Resource.getText(Resource.toUrl("square-checkout.html", getClass()));
                    ctx.response().headers().set("Content-Type", "text/html; charset=utf-8");
                    ctx.response().setStatusCode(200).end(html);
                });
    }

    @Override
    public String getName() {
        return GATEWAY_NAME;
    }

    @Override
    public Future<GatewayInitiatePaymentResult> initiatePayment(GatewayInitiatePaymentArgument argument) {
        return Future.succeededFuture(new GatewayInitiatePaymentResult(SQUARE_EMBEDDED_PAYMENT_URL, false));
    }

    @Override
    public Future<GatewayMakeApiPaymentResult> makeApiPayment(GatewayMakeApiPaymentArgument argument) {
        return Future.failedFuture("makeApiPayment() not yet implemented for Square");
    }
}
