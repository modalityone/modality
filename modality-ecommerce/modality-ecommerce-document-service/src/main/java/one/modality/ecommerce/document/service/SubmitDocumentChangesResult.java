package one.modality.ecommerce.document.service;

/**
 * @author Bruno Salmon
 */
public record SubmitDocumentChangesResult(
    Object documentPrimaryKey,
    Object documentRef,
    Object cartPrimaryKey,
    String cartUuid) {

}
