package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public record UpdatePaymentStatusArgument(
    Object paymentPrimaryKey,
    String gatewayResponse,
    String gatewayTransactionRef,
    String gatewayStatus,
    boolean isPendingStatus,
    boolean isSuccessfulStatus,
    boolean isExplicitUserCancellation,
    String errorMessage
) {

    public static UpdatePaymentStatusArgument createCapturedStatusArgument(Object paymentPrimaryKey, String gatewayResponse, String gatewayTransactionRef, String gatewayStatus, boolean pendingStatus, boolean successStatus) {
        return new UpdatePaymentStatusArgument(paymentPrimaryKey, gatewayResponse, gatewayTransactionRef, gatewayStatus, pendingStatus, successStatus, false, null);
    }

    public static UpdatePaymentStatusArgument createCancelStatusArgument(Object paymentPrimaryKey, boolean explicitUserCancellation) {
        return new UpdatePaymentStatusArgument(paymentPrimaryKey, null, null, null, false, false, explicitUserCancellation, null);
    }

    public static UpdatePaymentStatusArgument createExceptionStatusArgument(Object paymentPrimaryKey, String gatewayResponse, String errorMessage) {
        return new UpdatePaymentStatusArgument(paymentPrimaryKey, gatewayResponse, null, null, true, false, false, errorMessage);
    }

}
