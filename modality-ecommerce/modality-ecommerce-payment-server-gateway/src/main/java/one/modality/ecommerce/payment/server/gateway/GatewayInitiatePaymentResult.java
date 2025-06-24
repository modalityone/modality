package one.modality.ecommerce.payment.server.gateway;

import one.modality.ecommerce.payment.SandboxCard;

/**
 * @author Bruno Salmon
 */
public final class GatewayInitiatePaymentResult {

    private final boolean live; // indicates if it's a live payment (false indicates a test / sandbox payment)
    private final boolean seamless; // indicates if the HTML content can be integrated seamlessly in the browser page
    private final String htmlContent; // Direct HTML content that can handle the payment (CC details, etc...) in an embedded WebView (ex: Stripe)
    private final String url; // URL of the page that can handle the payment (redirect will tell what to do with it)
    private final boolean redirect; // true => URL needs to be opened in a separate browser window, false => URL can be opened in an embedded WebView (ex: Square)
    private final boolean hasHtmlPayButton; // indicates if a "Pay" button is already integrated in the gateway HTML code
    private final SandboxCard[] sandboxCards;

    public GatewayInitiatePaymentResult(boolean live, boolean seamless, String htmlContent, String url, boolean redirect, boolean hasHtmlPayButton, SandboxCard[] sandboxCards) {
        this.live = live;
        this.seamless = seamless;
        this.htmlContent = htmlContent;
        this.url = url;
        this.redirect = redirect;
        this.hasHtmlPayButton = hasHtmlPayButton;
        this.sandboxCards = sandboxCards;
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

    public boolean hasHtmlPayButton() {
        return hasHtmlPayButton;
    }

    public SandboxCard[] getSandboxCards() {
        return sandboxCards;
    }

    /*=========================================== Static factory methods =============================================*/

    /*================================================ Redirect API ==================================================*/
    // => payment page hosted by the gateway company

    public static GatewayInitiatePaymentResult createLiveRedirectInitiatePaymentResult(boolean seamless, String url) {
        return createRedirectInitiatePaymentResult(true, seamless, url, null);
    }

    public static GatewayInitiatePaymentResult createSandboxRedirectInitiatePaymentResult(boolean seamless, String url, SandboxCard[] sandboxCards) {
        return createRedirectInitiatePaymentResult(false, seamless, url, sandboxCards);
    }

    public static GatewayInitiatePaymentResult createRedirectInitiatePaymentResult(boolean live, boolean seamless, String url, SandboxCard[] sandboxCards) {
        return new GatewayInitiatePaymentResult(live, seamless, null, url, true, true, sandboxCards);
    }

    /*========================================= Embedded API (HTML content)  =========================================*/
    // => payment hosted by the app with provided HTML content, eventually seamlessly if possible

    public static GatewayInitiatePaymentResult createLiveEmbeddedContentInitiatePaymentResult(boolean seamless, String htmlContent, boolean hasHtmlPayButton) {
        return createEmbeddedContentInitiatePaymentResult(true, seamless, htmlContent, hasHtmlPayButton, null);
    }

    public static GatewayInitiatePaymentResult createSandboxEmbeddedContentInitiatePaymentResult(boolean seamless, String htmlContent, boolean hasHtmlPayButton, SandboxCard[] sandboxCards) {
        return createEmbeddedContentInitiatePaymentResult(false, seamless, htmlContent, hasHtmlPayButton, sandboxCards);
    }

    public static GatewayInitiatePaymentResult createEmbeddedContentInitiatePaymentResult(boolean live, boolean seamless, String htmlContent, boolean hasHtmlPayButton, SandboxCard[] sandboxCards) {
        return new GatewayInitiatePaymentResult(live, seamless, htmlContent, null, false, hasHtmlPayButton, sandboxCards);
    }

    /*========================================= Embedded API (URL)  =========================================*/
    // => payment hosted by the app with provided URL, eventually seamlessly if possible

    public static GatewayInitiatePaymentResult createLiveEmbeddedUrlInitiatePaymentResult(boolean seamless, String url, boolean hasHtmlPayButton) {
        return createEmbeddedUrlInitiatePaymentResult(true, seamless, url, hasHtmlPayButton, null);
    }

    public static GatewayInitiatePaymentResult createSandboxEmbeddedUrlInitiatePaymentResult(boolean seamless, String url, boolean hasHtmlPayButton, SandboxCard[] sandboxCards) {
        return createEmbeddedUrlInitiatePaymentResult(false, seamless, url, hasHtmlPayButton, sandboxCards);
    }


    public static GatewayInitiatePaymentResult createEmbeddedUrlInitiatePaymentResult(boolean live, boolean seamless, String url, boolean hasHtmlPayButton, SandboxCard[] sandboxCards) {
        return new GatewayInitiatePaymentResult(live, seamless, null, url, false, hasHtmlPayButton, sandboxCards);
    }

}
