package one.modality.ecommerce.payment.server.gateway;

import java.util.Map;

/**
 * @author Bruno Salmon
 */
public final class GatewayInitiatePaymentArgument {

    private final String paymentId; // pass as a String to make it easier to manage for the gateway
    private final long amount;
    private final String currencyCode;
    private final boolean live;
    private final boolean seamlessIfSupported;
    private final String productName;
    private final Map<String, String> accountParameters;

    public GatewayInitiatePaymentArgument(String paymentId, long amount, String currencyCode, boolean live, boolean seamlessIfSupported, String productName, Map<String, String> accountParameters) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.live = live;
        this.seamlessIfSupported = seamlessIfSupported;
        this.productName = productName;
        this.accountParameters = accountParameters;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public boolean isLive() {
        return live;
    }

    public boolean isSeamlessIfSupported() {
        return seamlessIfSupported;
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
