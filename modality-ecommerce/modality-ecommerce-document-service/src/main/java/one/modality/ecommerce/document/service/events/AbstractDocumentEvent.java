package one.modality.ecommerce.document.service.events;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractDocumentEvent extends AbstractSourceEvent {

    protected Document document;       // Working entity on client-side only (not serialised)
    private Object documentPrimaryKey; // Its primary key (serialised)

    public AbstractDocumentEvent(Object documentPrimaryKey) {
        this.documentPrimaryKey = documentPrimaryKey;
    }

    public AbstractDocumentEvent(Document document) {
        this.document = document;
        this.documentPrimaryKey = Entities.getPrimaryKey(document);
    }

    public Document getDocument() {
        if (document == null && entityStore != null) {
            document = entityStore.getEntity(Document.class, documentPrimaryKey);
        }
        return document;
    }

    public Object getDocumentPrimaryKey() {
        return documentPrimaryKey;
    }

    public void setDocumentPrimaryKey(Object documentPrimaryKey) { // Used to refactor new primary keys once inserted on server
        this.documentPrimaryKey = documentPrimaryKey;
    }
}
