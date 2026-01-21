package one.modality.ecommerce.document.service;

/**
 * @author Bruno Salmon
 */
public record SubmitDocumentChangesResult(
    DocumentChangesStatus status,
    // When status = APPROVED, these fields contain some useful information related to the document
    Object documentPrimaryKey,
    Object documentRef,
    Object cartPrimaryKey,
    String cartUuid,
    // When status = SOLD_OUT, these fields report the site and item primary keys that caused the failure
    Object soldOutSitePrimaryKey,
    Object soldOutItemPrimaryKey,
    // When status = ENQUEUED,
    Object queueToken
) {

    public static SubmitDocumentChangesResult createApprovedResult(Object documentPrimaryKey, Object documentRef, Object cartPrimaryKey, String cartUuid) {
        return new SubmitDocumentChangesResult(DocumentChangesStatus.APPROVED, documentPrimaryKey, documentRef, cartPrimaryKey, cartUuid, null, null, null);
    }

    public static SubmitDocumentChangesResult createSoldOutResult(Object soldOutSitePrimaryKey, Object soldOutItemPrimaryKey) {
        return new SubmitDocumentChangesResult(DocumentChangesStatus.SOLD_OUT, null, null, null, null, soldOutSitePrimaryKey, soldOutItemPrimaryKey, null);
    }

    public static SubmitDocumentChangesResult createEnqueuedResult(Object queueToken) {
        return new SubmitDocumentChangesResult(DocumentChangesStatus.ENQUEUED, null, null, null, null, null, null, queueToken);
    }

    public static SubmitDocumentChangesResult withQueueToken(SubmitDocumentChangesResult result, Object queueToken) {
        return new SubmitDocumentChangesResult(result.status, result.documentPrimaryKey, result.documentRef, result.cartPrimaryKey, result.cartUuid, result.soldOutSitePrimaryKey, result.soldOutItemPrimaryKey, queueToken);
    }
}
