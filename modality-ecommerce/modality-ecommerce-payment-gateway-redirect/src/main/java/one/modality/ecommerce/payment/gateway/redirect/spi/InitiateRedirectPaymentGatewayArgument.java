package one.modality.ecommerce.payment.gateway.redirect.spi;

import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentArgument;

/**
 * @author Bruno Salmon
 */
public class InitiateRedirectPaymentGatewayArgument {

    private final InitiateRedirectPaymentArgument userArgument;
    private final String accountId;
    private final String successUrl;
    private final String cancelUrl;

    public InitiateRedirectPaymentGatewayArgument(InitiateRedirectPaymentArgument userArgument, String accountId, String successUrl, String cancelUrl) {
        this.userArgument = userArgument;
        this.accountId = accountId;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    public InitiateRedirectPaymentArgument getUserArgument() {
        return userArgument;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }
}
