package one.modality.ecommerce.payment;

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
        return this == PENDING || this == APPROVED;
    }

    public boolean isSuccessful() {
        return this == APPROVED || this == COMPLETED;
    }

    public boolean isFailed() {
        return this == CANCELED || this == FAILED;
    }

}
