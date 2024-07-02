package one.modality.ecommerce.payment.server.gateway;

/**
 * @author Bruno Salmon
 */
public final class GatewayInitiatePaymentResult {

    private final boolean live; // indicates if it's a live payment (false indicates a test / sandbox payment)
    private final String htmlContent; // Direct HTML content that can handle the payment (CC details, etc...) in an embedded WebView (ex: Stripe)
    private final String url; // URL of the page that can handle the payment (redirect will tell what to do with it)
    private final boolean redirect; // true => URL needs to be opened in a separate browser window, false => URL can be opened in an embedded WebView (ex: Square)

    public GatewayInitiatePaymentResult(boolean live, String htmlContent, String url, boolean redirect) {
        this.live = live;
        this.htmlContent = htmlContent;
        this.url = url;
        this.redirect = redirect;
    }

    public boolean isLive() {
        return live;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public String getUrl() {
        return url;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public static GatewayInitiatePaymentResult createRedirectInitiatePaymentResult(boolean live, String url) {
        return new GatewayInitiatePaymentResult(live, null, url, true);
    }

    public static GatewayInitiatePaymentResult createEmbeddedContentInitiatePaymentResult(boolean live, String htmlContent) {
        return new GatewayInitiatePaymentResult(live, htmlContent, null, false);
    }

    public static GatewayInitiatePaymentResult createEmbeddedUrlInitiatePaymentResult(boolean live, String url) {
        return new GatewayInitiatePaymentResult(live, null, url, false);
    }

}
