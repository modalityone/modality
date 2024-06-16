package one.modality.ecommerce.payment.gateway;

/**
 * @author Bruno Salmon
 */
public final class GatewayInitiatePaymentResult {

    private final String htmlContent; // Direct HTML content that can handle the payment (CC details, etc...) in an embedded WebView (ex: Stripe)
    private final String url; // URL of the page that can handle the payment (redirect will tell what to do with it)
    private final boolean redirect; // true => URL needs to be opened in a separate browser window, false => URL can be opened in an embedded WebView (ex: Square)

    public GatewayInitiatePaymentResult(String htmlContent, String url, boolean redirect) {
        this.htmlContent = htmlContent;
        this.url = url;
        this.redirect = redirect;
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

    public static GatewayInitiatePaymentResult createRedirectInitiatePaymentResult(String url) {
        return new GatewayInitiatePaymentResult(null, url, true);
    }

    public static GatewayInitiatePaymentResult createEmbeddedContentInitiatePaymentResult(String htmlContent) {
        return new GatewayInitiatePaymentResult(htmlContent, null, false);
    }

    public static GatewayInitiatePaymentResult createEmbeddedUrlInitiatePaymentResult(String url) {
        return new GatewayInitiatePaymentResult(null, url, false);
    }

}
