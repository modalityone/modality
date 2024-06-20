package one.modality.ecommerce.payment;

/**
 * @author Bruno Salmon
 */
public final class UpdatePaymentStatusArgument {

    private final String paymentId;
    private final String wholeResponse;
    private final String transactionRef;
    private final String status;
    private final boolean successStatus;
    private final String error;

    public UpdatePaymentStatusArgument(String paymentId, String wholeResponse, String transactionRef, String status, boolean successStatus, String error) {
        this.paymentId = paymentId;
        this.wholeResponse = wholeResponse;
        this.transactionRef = transactionRef;
        this.status = status;
        this.successStatus = successStatus;
        this.error = error;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getWholeResponse() {
        return wholeResponse;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public String getStatus() {
        return status;
    }

    public boolean isSuccessStatus() {
        return successStatus;
    }

    public String getError() {
        return error;
    }

    public static UpdatePaymentStatusArgument createSuccessStatusArgument(String paymentId, String wholeResponse, String transactionRef, String status, boolean successStatus) {
        return new UpdatePaymentStatusArgument(paymentId, wholeResponse, transactionRef, status,successStatus, null);
    }

    public static UpdatePaymentStatusArgument createErrorStatusArgument(String paymentId, String wholeResponse, String error) {
        return new UpdatePaymentStatusArgument(paymentId, wholeResponse, null, null, false, error);
    }

}
