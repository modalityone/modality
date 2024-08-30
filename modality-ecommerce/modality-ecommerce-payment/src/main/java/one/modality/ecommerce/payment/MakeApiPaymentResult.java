package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public class MakeApiPaymentResult {

    private final boolean success;

    public MakeApiPaymentResult(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

}
