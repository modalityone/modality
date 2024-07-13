package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class UpdatePaymentStatusArgument {

    private final Object paymentPrimaryKey;
    private final String gatewayResponse;
    private final String gatewayTransactionRef;
    private final String gatewayStatus;
    private final boolean pendingStatus;
    private final boolean successfulStatus;
    private final String errorMessage;

    public UpdatePaymentStatusArgument(Object paymentPrimaryKey, String gatewayResponse, String gatewayTransactionRef, String gatewayStatus, boolean pendingStatus, boolean successfulStatus, String errorMessage) {
        this.paymentPrimaryKey = paymentPrimaryKey;
        this.gatewayResponse = gatewayResponse;
        this.gatewayTransactionRef = gatewayTransactionRef;
        this.gatewayStatus = gatewayStatus;
        this.pendingStatus = pendingStatus;
        this.successfulStatus = successfulStatus;
        this.errorMessage = errorMessage;
    }

    public Object getPaymentPrimaryKey() {
        return paymentPrimaryKey;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public String getGatewayTransactionRef() {
        return gatewayTransactionRef;
    }

    public String getGatewayStatus() {
        return gatewayStatus;
    }

    public boolean isPendingStatus() {
        return pendingStatus;
    }

    public boolean isSuccessfulStatus() {
        return successfulStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static UpdatePaymentStatusArgument createCapturedStatusArgument(Object paymentPrimaryKey, String gatewayResponse, String gatewayTransactionRef, String gatewayStatus, boolean pendingStatus, boolean successStatus) {
        return new UpdatePaymentStatusArgument(paymentPrimaryKey, gatewayResponse, gatewayTransactionRef, gatewayStatus, pendingStatus, successStatus, null);
    }

    public static UpdatePaymentStatusArgument createCancelStatusArgument(Object paymentPrimaryKey) {
        return new UpdatePaymentStatusArgument(paymentPrimaryKey, null, null, null, false, false, null);
    }

    public static UpdatePaymentStatusArgument createExceptionStatusArgument(Object paymentPrimaryKey, String gatewayResponse, String errorMessage) {
        return new UpdatePaymentStatusArgument(paymentPrimaryKey, gatewayResponse, null, null, true, false, errorMessage);
    }

}
