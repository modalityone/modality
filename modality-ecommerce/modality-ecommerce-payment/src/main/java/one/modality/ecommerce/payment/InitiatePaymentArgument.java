package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class InitiatePaymentArgument {

    private final int amount;
    private final Object documentPrimaryKey;
    // TODO: spread payment over several bookings
    private final boolean seamlessIfSupported;

    public InitiatePaymentArgument(int amount, Object documentPrimaryKey, boolean seamlessIfSupported) {
        this.amount = amount;
        this.documentPrimaryKey = documentPrimaryKey;
        this.seamlessIfSupported = seamlessIfSupported;
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
}
