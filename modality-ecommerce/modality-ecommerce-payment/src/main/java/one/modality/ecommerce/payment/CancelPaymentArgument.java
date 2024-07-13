package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class CancelPaymentArgument {

    private final Object paymentPrimaryKey;

    public CancelPaymentArgument(Object paymentPrimaryKey) {
        this.paymentPrimaryKey = paymentPrimaryKey;
    }

    public Object getPaymentPrimaryKey() {
        return paymentPrimaryKey;
    }
}
