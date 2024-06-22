package one.modality.ecommerce.payment.client;

/**
 * @author Bruno Salmon
 */
public enum PaymentStatus {

    APPROVED,
    PENDING,
    COMPLETED,
    CANCELED,
    FAILED;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isSuccessful() {
        return this == APPROVED || this == COMPLETED;
    }

    public boolean isFailed() {
        return this == CANCELED || this == FAILED;
    }

}
