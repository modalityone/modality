package one.modality.ecommerce.payment.embedded;

/**
 * @author Bruno Salmon
 */
public final class InitiateEmbeddedPaymentArgument {

    private final long amount;
    private final String currency;
    private final String productName;
    private final long quantity;
    private final String customerId;
    private final String successUrl;
    private final String failUrl;

    public InitiateEmbeddedPaymentArgument(long amount, String currency, String productName, long quantity, String customerId, String successUrl, String failUrl) {
        this.amount = amount;
        this.currency = currency;
        this.productName = productName;
        this.quantity = quantity;
        this.customerId = customerId;
        this.successUrl = successUrl;
        this.failUrl = failUrl;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getProductName() {
        return productName;
    }

    public long getQuantity() {
        return quantity;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public String getFailUrl() {
        return failUrl;
    }
}
