package one.modality.ecommerce.payment;

/**
 * @param amount              The amount to pay in cents.
 * @param documentPrimaryKey  PK of the document to pay for (TODO: support spread payments over several documents)
 * @param preferredFormType   Indicates the preferred payment form type (embedded or redirected).
 * @param favorSeamless       Indicates that the client would prefer a seamless integration of the web payment form rather than an HTML code in an iFrame. If the Gateway supports seamless integration, it will send a GatewayInitiatePaymentResult with seamless = true, and htmlContent will actually contain the script to execute in seamless mode.
 * @param isOriginOnHttps     Indicates that the client already runs on https. If not, gateway may prefer to run the payment form in a secure iFrame, even if the client asked for a seamless integration (favorSeamless = true).
 *
 * @author Bruno Salmon
 */
public record InitiatePaymentArgument(
    int amount,
    Object documentPrimaryKey,
    PaymentFormType preferredFormType,
    boolean favorSeamless,
    boolean isOriginOnHttps
) { }
