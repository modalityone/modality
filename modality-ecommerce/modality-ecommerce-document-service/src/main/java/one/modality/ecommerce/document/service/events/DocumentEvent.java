package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public abstract class DocumentEvent extends SourceEvent {

    private final Document document;
    private final Object documentPrimaryKey;

    public DocumentEvent(Object documentPrimaryKey) {
        this.document = null;
        this.documentPrimaryKey = documentPrimaryKey;
    }

    public DocumentEvent(Document document) {
        // Document shouldn't be null, but ok to be null for now
        this.document = document;
        this.documentPrimaryKey = document == null ? null : document.getPrimaryKey();
    }

    public Document getDocument() {
        return document;
    }

    public Object getDocumentPrimaryKey() {
        return documentPrimaryKey;
    }

}
