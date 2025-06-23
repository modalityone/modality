package one.modality.ecommerce.payment.server.gateway;

import java.util.Map;

/**
 * @author Bruno Salmon
 */
public class GatewayCompletePaymentArgument {

    private final boolean live;
    private final String accessToken;
    private final String payload;
    // The above is enough for some payment gateways like Square, but not for others like Authorize.net where some
    // transaction settings (including payment amount) are set on completion and not on initialization, and additional
    // require API calls
    private final long amount;
    private final Map<String, String> accountParameters; // necessary to get the Gateway keys for API calls

    public GatewayCompletePaymentArgument(boolean live, String accessToken, String payload, long amount, Map<String, String> accountParameters) {
        this.live = live;
        this.accessToken = accessToken;
        this.payload = payload;
        this.amount = amount;
        this.accountParameters = accountParameters;
    }

    public boolean isLive() {
        return live;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getPayload() {
        return payload;
    }

    public long getAmount() {
        return amount;
    }

    public Map<String, String> getAccountParameters() {
        return accountParameters;
    }

    public String getAccountParameter(String key) {
        return accountParameters.get(key);
    }

}
