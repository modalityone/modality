package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class CompletePaymentResult {

    private final PaymentStatus paymentStatus;

    public CompletePaymentResult(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
}
