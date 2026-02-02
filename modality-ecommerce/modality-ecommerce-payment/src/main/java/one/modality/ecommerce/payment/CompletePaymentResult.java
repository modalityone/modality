package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public record CompletePaymentResult(
    PaymentStatus paymentStatus,
    PaymentFailureReason failureReason
) { }
