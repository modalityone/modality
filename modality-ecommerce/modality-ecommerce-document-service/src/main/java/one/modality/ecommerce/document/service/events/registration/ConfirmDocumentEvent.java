package one.modality.ecommerce.document.service.events.registration;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class ConfirmDocumentEvent extends AbstractDocumentEvent {

    private final boolean confirmed;
    private final boolean read;

    public ConfirmDocumentEvent(Object documentPrimaryKey, boolean confirmed) {
        this(documentPrimaryKey, confirmed, false);
    }

    public ConfirmDocumentEvent(Object documentPrimaryKey, boolean confirmed, boolean read) {
        super(documentPrimaryKey);
        this.confirmed = confirmed;
        this.read = read;
    }

    public ConfirmDocumentEvent(Document document, boolean confirmed) {
        this(document, confirmed, false);
    }

    public ConfirmDocumentEvent(Document document, boolean confirmed, boolean read) {
        super(document);
        this.confirmed = confirmed;
        this.read = read;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public boolean isRead() {
        return read;
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setConfirmed(confirmed);
        document.setRead(read);
    }
}
