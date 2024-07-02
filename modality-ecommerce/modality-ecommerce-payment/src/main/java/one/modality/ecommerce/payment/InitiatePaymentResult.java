package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class InitiatePaymentResult {

    private final Object paymentPrimaryKey; // PK of the generated payment in the database (MoneyTransfer in Modality)
    private final boolean live; // indicates if it's a live payment (false indicates a test / sandbox payment)
    private final String htmlContent; // Direct HTML content that can handle the payment (CC details, etc...) in an embedded WebView (ex: Stripe)
    private final String url; // URL of the page that can handle the payment (redirect will tell what to do with it)
    private final boolean redirect; // true => URL needs to be opened in a separate browser window, false => URL can be opened in an embedded WebView (ex: Square)

    public InitiatePaymentResult(Object paymentPrimaryKey, boolean live, String htmlContent, String url, boolean redirect) {
        this.paymentPrimaryKey = paymentPrimaryKey;
        this.live = live;
        this.htmlContent = htmlContent;
        this.url = url;
        this.redirect = redirect;
    }

    public Object getPaymentPrimaryKey() {
        return paymentPrimaryKey;
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

    public static InitiatePaymentResult createRedirectInitiatePaymentResult(Object paymentPrimaryKey, boolean live, String url) {
        return new InitiatePaymentResult(paymentPrimaryKey, live, null, url, true);
    }

    public static InitiatePaymentResult createEmbeddedContentInitiatePaymentResult(Object paymentPrimaryKey, boolean live, String htmlContent) {
        return new InitiatePaymentResult(paymentPrimaryKey, live, htmlContent, null, false);
    }

    public static InitiatePaymentResult createEmbeddedUrlInitiatePaymentResult(Object paymentPrimaryKey, boolean live, String url) {
        return new InitiatePaymentResult(paymentPrimaryKey, live, null, url, false);
    }

}
