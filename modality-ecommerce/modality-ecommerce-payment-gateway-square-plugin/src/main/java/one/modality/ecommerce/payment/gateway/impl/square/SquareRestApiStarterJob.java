package one.modality.ecommerce.payment.gateway.impl.square;

import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.vertx.common.VertxInstance;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;

/**
 * @author Bruno Salmon
 */
public final class SquareRestApiStarterJob implements ApplicationJob {

    static final String SQUARE_EMBEDDED_PAYMENT_URL = "/payment/square/initiatePayment/:responseCacheKey";

    @Override
    public void onInit() {
        Router router = VertxInstance.getHttpRouter();
        router.route(SQUARE_EMBEDDED_PAYMENT_URL)
                .handler(ctx -> {
                    String cacheKey = ctx.pathParam("responseCacheKey");
                    String html = SquareRestApiOneTimeHtmlResponsesCache.getOneTimeHtmlResponse(cacheKey);
                    ctx.response()
                            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML)
                            .end(html);
                });
    }
}
