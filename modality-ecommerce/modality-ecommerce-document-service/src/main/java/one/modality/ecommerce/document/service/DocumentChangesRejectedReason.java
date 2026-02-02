package one.modality.ecommerce.document.service;

/**
 * @author Bruno Salmon
 */
public enum DocumentChangesRejectedReason {

    SOLD_OUT,

    ALREADY_BOOKED,

    EVENT_ON_HOLD,

    TECHNICAL_ERROR // Used only when SubmitDocumentChangesArgument has been enqueued and the final result is pushed,
    // otherwise (when not enqueued and processed immediately) the application code should use Future.onFailure() to
    // handle technical errors.

}
