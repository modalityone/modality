package one.modality.ecommerce.payment.server.gateway;

import one.modality.ecommerce.payment.PaymentFormType;

import java.util.Map;

/**
 * @author Bruno Salmon
 */
public record GatewayInitiatePaymentArgument(
    GatewayItem item,
    String currencyCode,
    boolean isLive, // if false, the payment gateway will use its sandbox environment for this payment
    PaymentFormType preferredFormType,
    boolean favorSeamless,
    boolean isOriginOnHttps,
    Map<String, String> accountParameters
) {

    public String getAccountParameter(String key, String defaultValue) {
        String value = getAccountParameter(key);
        return value == null ? defaultValue : value;
    }

    public String getAccountParameter(String key) {
        return accountParameters.get(key);
    }

    public String getRequiredAccountParameter(String key) throws IllegalArgumentException {
        String value = getAccountParameter(key);
        if (value != null)
            return value;
        throw new IllegalArgumentException("Missing required account parameter: " + key);
    }

}
