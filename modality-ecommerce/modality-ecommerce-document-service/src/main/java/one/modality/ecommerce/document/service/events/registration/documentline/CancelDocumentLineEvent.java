package one.modality.ecommerce.document.service.events.registration.documentline;

import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class CancelDocumentLineEvent extends AbstractDocumentLineEvent {

    private final boolean cancelled;
    private final boolean read;

    public CancelDocumentLineEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, boolean cancelled, boolean read) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.cancelled = cancelled;
        this.read = read;
    }

    public CancelDocumentLineEvent(DocumentLine documentLine, boolean cancelled, boolean read) {
        super(documentLine);
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
    public void replayEventOnDocumentLine() {
        super.replayEventOnDocumentLine();
        documentLine.setCancelled(cancelled);
        documentLine.setRead(read);
    }
}
