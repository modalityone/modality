package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class CancelPaymentResult {

    private final boolean bookingCancelled;

    public CancelPaymentResult(boolean bookingCancelled) {
        this.bookingCancelled = bookingCancelled;
    }

    public boolean isBookingCancelled() {
        return bookingCancelled;
    }
}
