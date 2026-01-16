package one.modality.ecommerce.document.service;

/**
 * @author Bruno Salmon
 */
public record SubmitDocumentChangesResult(
    // On changes submit success, these fields report some useful information related to the document
    Object documentPrimaryKey,
    Object documentRef,
    Object cartPrimaryKey,
    String cartUuid,
    // On database submit failure due to a sold-out item, these fields report the site and item primary keys that caused the failure
    boolean soldOut,
    Object soldOutSitePrimaryKey,
    Object soldOutItemPrimaryKey
) {

    public boolean isSuccessfullySubmitted() {
        return documentPrimaryKey != null;
    }

    public boolean isSoldOut() {
        return soldOut;
    }
}
