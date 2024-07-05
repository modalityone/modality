package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class InitiatePaymentResult {

    private final Object paymentPrimaryKey; // PK of the generated payment in the database (MoneyTransfer in Modality)
    private final boolean live; // indicates if it's a live payment (false indicates a test / sandbox payment)
    private final boolean seamless; // indicates if the html content can be integrated seamlessly in the browser page
    private final String htmlContent; // Direct HTML content that can handle the payment (CC details, etc...) in an embedded WebView (ex: Stripe)
    private final String url; // URL of the page that can handle the payment (redirect will tell what to do with it)
    private final boolean redirect; // true => URL needs to be opened in a separate browser window, false => URL can be opened in an embedded WebView (ex: Square)
    private final SandboxCard[] sandboxCards;

    public InitiatePaymentResult(Object paymentPrimaryKey, boolean live, boolean seamless, String htmlContent, String url, boolean redirect, SandboxCard[] sandboxCards) {
        this.paymentPrimaryKey = paymentPrimaryKey;
        this.live = live;
        this.seamless = seamless;
        this.htmlContent = htmlContent;
        this.url = url;
        this.redirect = redirect;
        this.sandboxCards = sandboxCards;
    }

    public Object getPaymentPrimaryKey() {
        return paymentPrimaryKey;
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

    public SandboxCard[] getSandboxCards() {
        return sandboxCards;
    }

    public static InitiatePaymentResult createLiveRedirectInitiatePaymentResult(Object paymentPrimaryKey, boolean seamless, String url) {
        return createRedirectInitiatePaymentResult(paymentPrimaryKey, true, seamless, url, null);
    }

    public static InitiatePaymentResult createSandboxRedirectInitiatePaymentResult(Object paymentPrimaryKey, boolean seamless, String url, SandboxCard[] sandboxCards) {
        return createRedirectInitiatePaymentResult(paymentPrimaryKey, false, seamless, url, sandboxCards);
    }

    public static InitiatePaymentResult createRedirectInitiatePaymentResult(Object paymentPrimaryKey, boolean live, boolean seamless, String url, SandboxCard[] sandboxCards) {
        return new InitiatePaymentResult(paymentPrimaryKey, live, seamless, null, url, true, sandboxCards);
    }

    public static InitiatePaymentResult createLiveEmbeddedContentInitiatePaymentResult(Object paymentPrimaryKey, boolean seamless, String htmlContent) {
        return createEmbeddedContentInitiatePaymentResult(paymentPrimaryKey, true, seamless, htmlContent, null);
    }

    public static InitiatePaymentResult createSandboxEmbeddedContentInitiatePaymentResult(Object paymentPrimaryKey, boolean seamless, String htmlContent, SandboxCard[] sandboxCards) {
        return createEmbeddedContentInitiatePaymentResult(paymentPrimaryKey, false, seamless, htmlContent, sandboxCards);
    }

    public static InitiatePaymentResult createEmbeddedContentInitiatePaymentResult(Object paymentPrimaryKey, boolean live, boolean seamless, String htmlContent, SandboxCard[] sandboxCards) {
        return new InitiatePaymentResult(paymentPrimaryKey, live, seamless, htmlContent, null, false, sandboxCards);
    }

    public static InitiatePaymentResult createLiveEmbeddedUrlInitiatePaymentResult(Object paymentPrimaryKey, boolean seamless, String url) {
        return createEmbeddedUrlInitiatePaymentResult(paymentPrimaryKey, true, seamless, url, null);
    }

    public static InitiatePaymentResult createSandboxEmbeddedUrlInitiatePaymentResult(Object paymentPrimaryKey, boolean seamless, String url, SandboxCard[] sandboxCards) {
        return createEmbeddedUrlInitiatePaymentResult(paymentPrimaryKey, false, seamless, url, sandboxCards);
    }

    public static InitiatePaymentResult createEmbeddedUrlInitiatePaymentResult(Object paymentPrimaryKey, boolean live, boolean seamless, String url, SandboxCard[] sandboxCards) {
        return new InitiatePaymentResult(paymentPrimaryKey, live, seamless, null, url, false, sandboxCards);
    }

}
