package one.modality.ecommerce.payment;

/**
 * @param gatewayName       Name of the payment gateway (ex: Square, Stripe, etc...)
 * @param paymentPrimaryKey PK of the generated payment in the database (MoneyTransfer in Modality)
 * @param amount            Amount of the payment (same as in InitiatePaymentArgument and also stored in the database)
 * @param isLive            Indicates if it's a live payment (false indicates a test / sandbox payment)
 * @param url               URL of the page that can handle the payment (redirect will tell what to do with it)
 * @param formType          Indicates the payment form type (embedded or redirected)
 * @param htmlContent       Direct HTML content that can handle the payment (CC details, etc...) in an embedded WebView (ex: Stripe)
 * @param isSeamless        indicates if the HTML content can be integrated seamlessly in the browser page
 * @param hasHtmlPayButton  indicates if a "Pay" button is already integrated in the gateway HTML code
 *
 * @author Bruno Salmon
 */
public record InitiatePaymentResult(
    String gatewayName,
    Object paymentPrimaryKey,
    int amount,
    boolean isLive,
    String url,
    PaymentFormType formType,
    // The following fields are only used when the payment form type is EMBEDDED
    String htmlContent,
    boolean isSeamless,
    boolean hasHtmlPayButton,
    SandboxCard[] sandboxCards
) { }
