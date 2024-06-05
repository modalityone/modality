package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public class MakeApiPaymentArgument {

    private final int amount;
    private final Object documentPrimaryKey;
    // TODO: spread payment over several bookings
    private final String ccNumber;
    private final String ccExpiry;

    public MakeApiPaymentArgument(int amount, Object documentPrimaryKey, String ccNumber, String ccExpiry) {
        this.amount = amount;
        this.documentPrimaryKey = documentPrimaryKey;
        this.ccNumber = ccNumber;
        this.ccExpiry = ccExpiry;
    }

    public int getAmount() {
        return amount;
    }

    public Object getDocumentPrimaryKey() {
        return documentPrimaryKey;
    }

    public String getCcNumber() {
        return ccNumber;
    }

    public String getCcExpiry() {
        return ccExpiry;
    }

}
