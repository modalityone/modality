package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class UpdatePaymentStatusArgument {

    private final String paymentId;
    private final String gatewayResponse;
    private final String gatewayTransactionRef;
    private final String gatewayStatus;
    private final boolean pendingStatus;
    private final boolean successfulStatus;
    private final String errorMessage;

    public UpdatePaymentStatusArgument(String paymentId, String gatewayResponse, String gatewayTransactionRef, String gatewayStatus, boolean pendingStatus, boolean successfulStatus, String errorMessage) {
        this.paymentId = paymentId;
        this.gatewayResponse = gatewayResponse;
        this.gatewayTransactionRef = gatewayTransactionRef;
        this.gatewayStatus = gatewayStatus;
        this.pendingStatus = pendingStatus;
        this.successfulStatus = successfulStatus;
        this.errorMessage = errorMessage;
    }

    public String getPaymentId() {
        return paymentId;
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

    public static UpdatePaymentStatusArgument createCapturedStatusArgument(String paymentId, String gatewayResponse, String gatewayTransactionRef, String gatewayStatus, boolean pendingStatus, boolean successStatus) {
        return new UpdatePaymentStatusArgument(paymentId, gatewayResponse, gatewayTransactionRef, gatewayStatus, pendingStatus, successStatus, null);
    }

    public static UpdatePaymentStatusArgument createExceptionStatusArgument(String paymentId, String gatewayResponse, String errorMessage) {
        return new UpdatePaymentStatusArgument(paymentId, gatewayResponse, null, null, true, false, errorMessage);
    }

}
