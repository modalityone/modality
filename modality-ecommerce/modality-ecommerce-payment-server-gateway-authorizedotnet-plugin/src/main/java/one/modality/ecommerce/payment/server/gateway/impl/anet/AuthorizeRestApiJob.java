package one.modality.ecommerce.payment.server.gateway.impl.anet;

import dev.webfx.platform.boot.spi.ApplicationJob;
import dev.webfx.platform.vertx.common.VertxInstance;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;

/**
 * @author Bruno Salmon
 */
public final class AuthorizeRestApiJob implements ApplicationJob {

    static final String AUTHORIZE_PAYMENT_FORM_ENDPOINT = "/payment/authorize/paymentForm/:htmlCacheKey";

    @Override
    public void onInit() {
        Router router = VertxInstance.getHttpRouter();

        /*====================================== EMBED PAYMENT FORM REST API =========================================*/

        // This endpoint is called by the Modality front-office when the web payment form can't be initialized through
        // some direct HTML content, but requires this HTML content to be loaded through a later server REST call.
        // Typically, when it is embedded in an iFrame, iFrame.src is set to that endpoint to load the HTML code and
        // start the web payment form.
        router.route(AUTHORIZE_PAYMENT_FORM_ENDPOINT)
            .handler(ctx -> {
                // Because it is a later call just after AuthorizePaymentGateway.initiatePayment(), we expect the
                // content to be present in the HTML cache, as set by initiatePayment() just before.
                String cacheKey = ctx.pathParam("htmlCacheKey");
                String html = AuthorizeRestApiOneTimeHtmlResponsesCache.getOneTimeHtmlResponse(cacheKey);
                // And we return that content
                ctx.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML)
                    .end(html);
            });
    }

}