package one.modality.ecommerce.payment.server.gateway;

/**
 * @author Bruno Salmon
 */
public final class GatewayInitiatePaymentResult {

    private final boolean live; // indicates if it's a live payment (false indicates a test / sandbox payment)
    private final boolean seamless; // indicates if the html content can be integrated seamlessly in the browser page
    private final String htmlContent; // Direct HTML content that can handle the payment (CC details, etc...) in an embedded WebView (ex: Stripe)
    private final String url; // URL of the page that can handle the payment (redirect will tell what to do with it)
    private final boolean redirect; // true => URL needs to be opened in a separate browser window, false => URL can be opened in an embedded WebView (ex: Square)

    public GatewayInitiatePaymentResult(boolean live, boolean seamless, String htmlContent, String url, boolean redirect) {
        this.live = live;
        this.seamless = seamless;
        this.htmlContent = htmlContent;
        this.url = url;
        this.redirect = redirect;
    }

    public boolean isLive() {
        return live;
    }

    public boolean isSeamless() {
        return seamless;
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

    public static GatewayInitiatePaymentResult createRedirectInitiatePaymentResult(boolean live, boolean seamless, String url) {
        return new GatewayInitiatePaymentResult(live, seamless, null, url, true);
    }

    public static GatewayInitiatePaymentResult createEmbeddedContentInitiatePaymentResult(boolean live, boolean seamless, String htmlContent) {
        return new GatewayInitiatePaymentResult(live, seamless, htmlContent, null, false);
    }

    public static GatewayInitiatePaymentResult createEmbeddedUrlInitiatePaymentResult(boolean live, boolean seamless, String url) {
        return new GatewayInitiatePaymentResult(live, seamless, null, url, false);
    }

}
