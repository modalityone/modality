package one.modality.ecommerce.payment.server.gateway;

import java.util.Map;

/**
 * @param accountParameters
 * @author Bruno Salmon
 */
public record GatewayCompletePaymentArgument(
    boolean isLive,
    String accessToken,
    String payload,
    // The above is enough for some payment gateways like Square, but not for others like Authorize.net where some
    // transaction settings (including payment amount) are set on completion and not on initialization.
    // The additional fields below  might be necessary in this case for the Gateway API calls.
    Map<String, String> accountParameters,
    long amount,
    GatewayCustomer customer,
    GatewayItem item
) {
    public String getAccountParameter(String key) {
        return accountParameters.get(key);
    }
}
