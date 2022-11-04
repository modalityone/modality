package one.modality.ecommerce.payment.direct;

/**
 * @author Bruno Salmon
 */
public class MakeDirectPaymentResult {

    private final boolean success;

    public MakeDirectPaymentResult(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

}
