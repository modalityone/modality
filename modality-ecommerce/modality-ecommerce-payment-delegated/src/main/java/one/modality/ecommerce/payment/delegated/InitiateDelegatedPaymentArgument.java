package one.modality.ecommerce.payment.delegated;

/**
 * @author Bruno Salmon
 */
public class InitiateDelegatedPaymentArgument {

    private final String description;
    private final int amount;
    private final String currency;

    public InitiateDelegatedPaymentArgument(String description, int amount, String currency) {
        this.description = description;
        this.amount = amount;
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

}
