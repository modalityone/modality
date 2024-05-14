package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.DocumentLine;

/**
 * @author Bruno Salmon
 */
public final class AddDocumentLineEvent extends DocumentLineEvent {

    private final Object sitePrimaryKey;
    private final Object itemPrimaryKey;

    public AddDocumentLineEvent(DocumentLine documentLine) {
        super(documentLine);
        sitePrimaryKey = documentLine.getSite().getPrimaryKey();
        itemPrimaryKey = documentLine.getItem().getPrimaryKey();
    }

    public AddDocumentLineEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, Object itemPrimaryKey, Object sitePrimaryKey) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.itemPrimaryKey = itemPrimaryKey;
        this.sitePrimaryKey = sitePrimaryKey;
    }
}
