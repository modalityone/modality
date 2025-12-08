package one.modality.ecommerce.payment.server.gateway;

import one.modality.ecommerce.payment.SandboxCard;

/**
 * @param isLive           indicates if it's a live payment (false indicates a test / sandbox payment)
 * @param url              URL of the page that can handle the payment (isEmbedded will tell what to do with it)
 * @param isEmbedded       false => URL needs to be opened in a separate browser window, true => URL can be opened in an embedded WebView (ex: Square)
 * @param htmlContent      Direct HTML content that can handle the payment (CC details, etc...) in an embedded WebView (ex: Stripe)
 * @param isSeamless       indicates if the HTML content can be integrated seamlessly in the browser page
 * @param hasHtmlPayButton indicates if a "Pay" button is already integrated in the gateway HTML code
 *
 * @author Bruno Salmon
 */
public record GatewayInitiatePaymentResult(
    boolean isLive,
    String url,
    boolean isEmbedded,
    // The following fields are only used when isEmbedded is true
    String htmlContent,
    boolean isSeamless,
    boolean hasHtmlPayButton,
    SandboxCard[] sandboxCards
) {

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
        return new GatewayInitiatePaymentResult(live, url, false, null, seamless, true, sandboxCards);
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
        return new GatewayInitiatePaymentResult(live, null, true, htmlContent, seamless, hasHtmlPayButton, sandboxCards);
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
        return new GatewayInitiatePaymentResult(live, url, true, null, seamless, hasHtmlPayButton, sandboxCards);
    }
}
