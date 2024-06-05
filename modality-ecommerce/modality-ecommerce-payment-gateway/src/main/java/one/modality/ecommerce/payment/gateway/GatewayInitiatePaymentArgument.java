package one.modality.ecommerce.payment.gateway;

/**
 * @author Bruno Salmon
 */
public final class GatewayInitiatePaymentArgument {

    private final long amount;
    private final String currency;
    private final String productName;
    private final String successUrl;
    private final String failUrl;

    public GatewayInitiatePaymentArgument(long amount, String currency, String productName, String successUrl, String failUrl) {
        this.amount = amount;
        this.currency = currency;
        this.productName = productName;
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

    public String getSuccessUrl() {
        return successUrl;
    }

    public String getFailUrl() {
        return failUrl;
    }

}
