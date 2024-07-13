package one.modality.ecommerce.payment.server.gateway;

import one.modality.ecommerce.payment.PaymentStatus;

/**
 * @author Bruno Salmon
 */
public class GatewayCompletePaymentResult {

    private final String gatewayResponse;
    private final String gatewayTransactionRef;
    private final String gatewayStatus;
    private final PaymentStatus paymentStatus;

    public GatewayCompletePaymentResult(String gatewayResponse, String gatewayTransactionRef, String gatewayStatus, PaymentStatus paymentStatus) {
        this.gatewayResponse = gatewayResponse;
        this.gatewayTransactionRef = gatewayTransactionRef;
        this.gatewayStatus = gatewayStatus;
        this.paymentStatus = paymentStatus;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public String getGatewayTransactionRef() {
        return gatewayTransactionRef;
    }

    public String getGatewayStatus() {
        return gatewayStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
}
