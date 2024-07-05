package one.modality.ecommerce.payment.server.gateway.impl.square;

import one.modality.ecommerce.payment.PaymentStatus;

/**
 * @author Bruno Salmon
 */
enum SquarePaymentStatus {

    // The payment has been created but has not yet been completed or captured. This status typically indicates that the payment is awaiting further action or processing.
    PENDING(PaymentStatus.PENDING),

    // The payment has been successfully completed. This status confirms that the funds have been captured and the transaction is finalized.
    COMPLETED(PaymentStatus.COMPLETED),

    // The payment has been authorized but not yet captured. This status indicates that the funds are reserved, and you have the option to capture or void the payment.
    APPROVED(PaymentStatus.APPROVED),

    // The payment was canceled before it could be completed. This can happen if the customer or the system cancels the transaction before finalizing.
    CANCELED(PaymentStatus.CANCELED),

    // The payment attempt failed. This status can occur due to various reasons such as insufficient funds, network issues, or declined transactions by the bank or payment processor.
    FAILED(PaymentStatus.FAILED),

    // The payment was successfully refunded. This status indicates that the transaction amount has been returned to the customer.
    REFUNDED(PaymentStatus.COMPLETED),

    // Part of the payment was refunded. This status indicates that a partial refund has been issued, but not the full amount of the original transaction.
    PARTIALLY_REFUNDED(PaymentStatus.COMPLETED),

    // The payment was voided. This status is typically used for payments that were authorized but then voided before capture, meaning no funds were transferred.
    VOIDED(PaymentStatus.FAILED),

    // Similar to "APPROVED," this status indicates that the payment has been authorized and is awaiting capture. This status might be used interchangeably depending on the specific API or context.
    AUTHORIZED(PaymentStatus.APPROVED);

    private final PaymentStatus genericPaymentStatus;

    SquarePaymentStatus(PaymentStatus genericPaymentStatus) {
        this.genericPaymentStatus = genericPaymentStatus;
    }

    public PaymentStatus getGenericPaymentStatus() {
        return genericPaymentStatus;
    }
}
