package one.modality.ecommerce.payment.server.gateway;

import one.modality.ecommerce.payment.PaymentFailureReason;
import one.modality.ecommerce.payment.PaymentStatus;

/**
 * @author Bruno Salmon
 */
public record GatewayCompletePaymentResult(
    String gatewayResponse,
    String gatewayTransactionRef,
    String gatewayStatus,
    PaymentStatus paymentStatus,
    PaymentFailureReason failureReason
) { }
