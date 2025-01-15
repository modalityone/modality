package one.modality.ecommerce.document.service.events;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractDocumentEvent extends AbstractSourceEvent {

    protected Document document;       // Working entity on client-side only (not serialised)
    private Object documentPrimaryKey; // Its primary key (serialised)

    private boolean playedOnDocument;

    public AbstractDocumentEvent(Object documentPrimaryKey) {
        this.documentPrimaryKey = documentPrimaryKey;
    }

    public AbstractDocumentEvent(Document document) {
        this.document = document;
        this.documentPrimaryKey = Entities.getPrimaryKey(document);
        setPlayed(true);
    }

    public Object getDocumentPrimaryKey() {
        return documentPrimaryKey;
    }

    public void setDocumentPrimaryKey(Object documentPrimaryKey) { // Used to refactor new primary keys once inserted on server
        this.documentPrimaryKey = documentPrimaryKey;
    }

    public Document getDocument() {
        if (document == null && entityStore != null) {
            createDocument();
        }
        if (document != null && !playedOnDocument) {
            replayEventOnDocument();
        }
        return document;
    }

    protected void createDocument() {
        if (isForSubmit())
            document = updateStore.updateEntity(Document.class, documentPrimaryKey);
        else
            document = entityStore.getOrCreateEntity(Document.class, documentPrimaryKey);
    }

    @Override
    public void replayEvent() {
        setPlayed(false);
        getDocument();
    }

    @Override
    public void setPlayed(boolean played) {
        playedOnDocument = played;
    }

    @Override
    public boolean isPlayed() {
        return playedOnDocument;
    }

    public void replayEventOnDocument() {
        playedOnDocument = true;
    }

}
