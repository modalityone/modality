package one.modality.ecommerce.payment.gateway;

import java.util.Map;

/**
 * @author Bruno Salmon
 */
public final class GatewayInitiatePaymentArgument {

    private final long amount;
    private final String currency;
    private final String productName;
    private final Map<String, String> accountParameters;

    public GatewayInitiatePaymentArgument(long amount, String currency, String productName, Map<String, String> accountParameters) {
        this.amount = amount;
        this.currency = currency;
        this.productName = productName;
        this.accountParameters = accountParameters;
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

    public Map<String, String> getAccountParameters() {
        return accountParameters;
    }

    public String getAccountParameter(String key) {
        return accountParameters.get(key);
    }
}
