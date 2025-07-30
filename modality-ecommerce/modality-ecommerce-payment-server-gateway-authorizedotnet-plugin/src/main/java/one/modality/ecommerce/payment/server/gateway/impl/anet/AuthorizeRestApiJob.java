package one.modality.ecommerce.payment.server.gateway.impl.anet;

import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.vertx.common.VertxInstance;
import dev.webfx.platform.util.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import one.modality.ecommerce.payment.server.gateway.impl.util.RestApiOneTimeHtmlResponsesCache;

/**
 * @author Bruno Salmon
 */
public final class AuthorizeRestApiJob implements ApplicationJob {

    static final String AUTHORIZE_PAYMENT_FORM_LOAD_ENDPOINT = "/payment/anet/loadPaymentForm/:htmlCacheKey";

    @Override
    public void onInit() {
        Router router = VertxInstance.getHttpRouter();

        /*====================================== EMBED PAYMENT FORM REST API =========================================*/

        // This endpoint is called by the Modality front-office when the web payment form can't be initialized through
        // some direct HTML content, but requires this HTML content to be loaded through a later server REST call.
        // Typically, when it is embedded in an iFrame, iFrame.src is set to that endpoint to load the HTML code and
        // start the web payment form.
        router.route(AUTHORIZE_PAYMENT_FORM_LOAD_ENDPOINT)
            .handler(ctx -> {
                // Because it is a later call just after AuthorizePaymentGateway.initiatePayment(), we expect the
                // content to be present in the HTML cache, as set by initiatePayment() just before.
                String cacheKey = ctx.pathParam("htmlCacheKey");
                String html = RestApiOneTimeHtmlResponsesCache.getOneTimeHtmlResponse(cacheKey);
                if (html != null) // We return that content if found
                    ctx.response()
                        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML)
                        .end(html);
                else // Not found
                    ctx.response()
                        .setStatusCode(HttpResponseStatus.BAD_REQUEST_400)
                        .end("No value for cache key: " + cacheKey);
            });
    }

}