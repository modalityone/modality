package one.modality.ecommerce.document.service;

/**
 * @author Bruno Salmon
 */
public final class SubmitDocumentChangesResult {

    private final Object documentPrimaryKey;
    private final Object documentRef;
    private final Object cartPrimaryKey;
    private final String cartUuid;

    public SubmitDocumentChangesResult(Object documentPrimaryKey, Object documentRef, Object cartPrimaryKey, String cartUuid) {
        this.documentPrimaryKey = documentPrimaryKey;
        this.documentRef = documentRef;
        this.cartPrimaryKey = cartPrimaryKey;
        this.cartUuid = cartUuid;
    }

    public Object getDocumentPrimaryKey() {
        return documentPrimaryKey;
    }

    public Object getDocumentRef() {
        return documentRef;
    }

    public Object getCartPrimaryKey() {
        return cartPrimaryKey;
    }

    public String getCartUuid() {
        return cartUuid;
    }
}
