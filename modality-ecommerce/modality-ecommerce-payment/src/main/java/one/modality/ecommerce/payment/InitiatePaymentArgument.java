package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class InitiatePaymentArgument {

    private final int amount;
    private final Object documentPrimaryKey;
    // TODO: spread payment over several bookings


    public InitiatePaymentArgument(int amount, Object documentPrimaryKey) {
        this.amount = amount;
        this.documentPrimaryKey = documentPrimaryKey;
    }

    public int getAmount() {
        return amount;
    }

    public Object getDocumentPrimaryKey() {
        return documentPrimaryKey;
    }
}
