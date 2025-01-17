package one.modality.ecommerce.document.service.events.book;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class CancelDocumentEvent extends AbstractDocumentEvent {

    private final boolean cancelled;
    private final boolean read;

    public CancelDocumentEvent(Object documentPrimaryKey, boolean cancelled) {
        this(documentPrimaryKey, cancelled, false);
    }

    public CancelDocumentEvent(Object documentPrimaryKey, boolean cancelled, boolean read) {
        super(documentPrimaryKey);
        this.cancelled = cancelled;
        this.read = read;
    }

    public CancelDocumentEvent(Document document, boolean cancelled) {
        this(document, cancelled, false);
    }

    public CancelDocumentEvent(Document document, boolean cancelled, boolean read) {
        super(document);
        this.cancelled = cancelled;
        this.read = read;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isRead() {
        return read;
    }

    @Override
    public void replayEventOnDocument() {
        document.setCancelled(cancelled);
        document.setRead(read);
        super.replayEventOnDocument(); // to set playedOnDocument = true
    }
}
