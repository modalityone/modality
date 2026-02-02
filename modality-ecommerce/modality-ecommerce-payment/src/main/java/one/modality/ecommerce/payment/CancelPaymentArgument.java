package one.modality.ecommerce.payment;

/**
 * @param isExplicitUserCancellation true = user pressed a Cancel button, false = user didn't finalize the payment process (ex: closed the window)
 *
 * @author Bruno Salmon
 */
public record CancelPaymentArgument(
    Object paymentPrimaryKey,
    boolean isExplicitUserCancellation
) { }
