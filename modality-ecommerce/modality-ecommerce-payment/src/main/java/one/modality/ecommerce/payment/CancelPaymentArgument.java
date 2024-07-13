package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class CancelPaymentArgument {

    private final Object paymentPrimaryKey;
    private final boolean explicitUserCancellation; // true = user pressed a Cancel button, false = user didn't finalise the payment process (ex: closed the window)

    public CancelPaymentArgument(Object paymentPrimaryKey, boolean explicitUserCancellation) {
        this.paymentPrimaryKey = paymentPrimaryKey;
        this.explicitUserCancellation = explicitUserCancellation;
    }

    public Object getPaymentPrimaryKey() {
        return paymentPrimaryKey;
    }

    public boolean isExplicitUserCancellation() {
        return explicitUserCancellation;
    }
}
