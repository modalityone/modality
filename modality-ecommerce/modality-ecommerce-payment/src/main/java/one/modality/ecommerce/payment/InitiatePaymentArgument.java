package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class InitiatePaymentArgument {

    private final int amount;
    private final Object documentPrimaryKey;
    // TODO: spread payment over several bookings
    // Indicates that the client would prefer a seamless integration of the web payment form rather than a html code
    // in an iFrame. If the Gateway support seamless integration, it will send a GatewayInitiatePaymentResult with
    // seamless = true, and htmlContent will actually contains the script to execute in seamless mode.
    private final boolean seamlessIfSupported;
    // Indicates that the client already runs on https. If not, the gateway may prefer to run the payment form in a
    // secure iFrame, even if the client asked a seamless integration (seamlessIfSupported = true).
    private final boolean parentPageHttps;

    public InitiatePaymentArgument(int amount, Object documentPrimaryKey, boolean seamlessIfSupported, boolean parentPageHttps) {
        this.amount = amount;
        this.documentPrimaryKey = documentPrimaryKey;
        this.seamlessIfSupported = seamlessIfSupported;
        this.parentPageHttps = parentPageHttps;
    }

    public int getAmount() {
        return amount;
    }

    public Object getDocumentPrimaryKey() {
        return documentPrimaryKey;
    }

    public boolean isSeamlessIfSupported() {
        return seamlessIfSupported;
    }

    public boolean isParentPageHttps() {
        return parentPageHttps;
    }
}
