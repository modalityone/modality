package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.DocumentLine;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractDocumentLineEvent extends AbstractDocumentEvent {

    private final DocumentLine documentLine;
    private final Object documentLinePrimaryKey;

    public AbstractDocumentLineEvent(Object documentPrimaryKey, Object documentLinePrimaryKey) {
        super(documentPrimaryKey);
        this.documentLine = null;
        this.documentLinePrimaryKey = documentLinePrimaryKey;
    }

    public AbstractDocumentLineEvent(DocumentLine documentLine) {
        super(documentLine.getDocument());
        this.documentLine = documentLine;
        this.documentLinePrimaryKey = documentLine.getPrimaryKey();
    }

    public DocumentLine getDocumentLine() {
        return documentLine;
    }

    public Object getDocumentLinePrimaryKey() {
        return documentLinePrimaryKey;
    }
}
