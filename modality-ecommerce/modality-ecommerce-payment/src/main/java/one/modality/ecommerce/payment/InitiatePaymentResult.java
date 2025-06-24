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
    private final boolean hasHtmlPayButton; // indicates if a "Pay" button is already integrated in the gateway HTML code
    private final String gatewayName;
    private final SandboxCard[] sandboxCards;

    public InitiatePaymentResult(Object paymentPrimaryKey, boolean live, boolean seamless, String htmlContent, String url, boolean redirect, boolean hasHtmlPayButton, String gatewayName, SandboxCard[] sandboxCards) {
        this.paymentPrimaryKey = paymentPrimaryKey;
        this.live = live;
        this.seamless = seamless;
        this.htmlContent = htmlContent;
        this.url = url;
        this.redirect = redirect;
        this.hasHtmlPayButton = hasHtmlPayButton;
        this.gatewayName = gatewayName;
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

    public boolean hasHtmlPayButton() {
        return hasHtmlPayButton;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public SandboxCard[] getSandboxCards() {
        return sandboxCards;
    }


}
