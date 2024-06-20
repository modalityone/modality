package one.modality.ecommerce.payment.gateway;

import java.util.Map;

/**
 * @author Bruno Salmon
 */
public final class GatewayInitiatePaymentArgument {

    private final String paymentId; // pass as a String to make it easier to manage for the gateway
    private final long amount;
    private final String currency;
    private final boolean live;
    private final String productName;
    private final Map<String, String> accountParameters;

    public GatewayInitiatePaymentArgument(String paymentId, long amount, String currency, boolean live, String productName, Map<String, String> accountParameters) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.live = live;
        this.productName = productName;
        this.accountParameters = accountParameters;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isLive() {
        return live;
    }

    public String getProductName() {
        return productName;
    }

    public Map<String, String> getAccountParameters() {
        return accountParameters;
    }

    public String getAccountParameter(String key) {
        return accountParameters.get(key);
    }
}
