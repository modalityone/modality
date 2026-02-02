package one.modality.ecommerce.document.service;

/**
 * @author Bruno Salmon
 */
public record SubmitDocumentChangesResult(
    DocumentChangesStatus status,
    DocumentChangesRejectedReason rejectedReason, // only when status = REJECTED
    // When status = APPROVED, these fields contain some useful information related to the document
    Object documentPrimaryKey,
    Object documentRef,
    Object cartPrimaryKey,
    String cartUuid,
    // When status = REJECTED and reason = SOLD_OUT, these fields report the site and item primary keys that caused the failure
    Object soldOutSitePrimaryKey,
    Object soldOutItemPrimaryKey,
    // When status = ENQUEUED, or when it's the final result pushed after the original request has been enqueued
    Object queueToken,
    String errorMessage // used when status = REJECTED and reason = TECHNICAL_ERROR
) {

    public static SubmitDocumentChangesResult createApprovedResult(Object documentPrimaryKey, Object documentRef, Object cartPrimaryKey, String cartUuid) {
        return new SubmitDocumentChangesResult(DocumentChangesStatus.APPROVED, null, documentPrimaryKey, documentRef, cartPrimaryKey, cartUuid, null, null, null, null);
    }

    public static SubmitDocumentChangesResult createSoldOutResult(Object soldOutSitePrimaryKey, Object soldOutItemPrimaryKey) {
        return new SubmitDocumentChangesResult(DocumentChangesStatus.REJECTED, DocumentChangesRejectedReason.SOLD_OUT, null, null, null, null, soldOutSitePrimaryKey, soldOutItemPrimaryKey, null, null);
    }

    public static SubmitDocumentChangesResult createAlreadyBookedResult() {
        return new SubmitDocumentChangesResult(DocumentChangesStatus.REJECTED, DocumentChangesRejectedReason.ALREADY_BOOKED, null, null, null, null, null, null, null, null);
    }

    public static SubmitDocumentChangesResult createEventOnHoldResult() {
        return new SubmitDocumentChangesResult(DocumentChangesStatus.REJECTED, DocumentChangesRejectedReason.EVENT_ON_HOLD, null, null, null, null, null, null, null, null);
    }

    public static SubmitDocumentChangesResult createEnqueuedResult(Object queueToken) {
        return new SubmitDocumentChangesResult(DocumentChangesStatus.ENQUEUED, null, null, null, null, null, null, null, queueToken, null);
    }

    public static SubmitDocumentChangesResult withQueueToken(SubmitDocumentChangesResult result, Object queueToken) {
        return new SubmitDocumentChangesResult(result.status, result.rejectedReason, result.documentPrimaryKey, result.documentRef, result.cartPrimaryKey, result.cartUuid, result.soldOutSitePrimaryKey, result.soldOutItemPrimaryKey, queueToken, null);
    }

    public static SubmitDocumentChangesResult technicalErrorResult(String errorMessage, Object queueToken) {
        return new SubmitDocumentChangesResult(DocumentChangesStatus.REJECTED, DocumentChangesRejectedReason.TECHNICAL_ERROR, null, null, null, null, null, null, queueToken, errorMessage);
    }

}
