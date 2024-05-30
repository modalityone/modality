package one.modality.ecommerce.payment.api;

/**
 * @author Bruno Salmon
 */
public class MakeApiPaymentArgument {

    private final int amount;
    private final String currency;
    private final String ccNumber;
    private final String ccExpiry;

    public MakeApiPaymentArgument(int amount, String currency, String ccNumber, String ccExpiry) {
        this.amount = amount;
        this.currency = currency;
        this.ccNumber = ccNumber;
        this.ccExpiry = ccExpiry;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCcNumber() {
        return ccNumber;
    }

    public String getCcExpiry() {
        return ccExpiry;
    }

}
