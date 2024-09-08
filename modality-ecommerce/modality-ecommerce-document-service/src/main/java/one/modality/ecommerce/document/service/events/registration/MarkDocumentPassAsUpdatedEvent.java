package one.modality.ecommerce.document.service.events.registration;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentPassAsUpdatedEvent extends AbstractDocumentEvent {

    private final boolean read;

    public MarkDocumentPassAsUpdatedEvent(Object documentPrimaryKey) {
        this(documentPrimaryKey, true);
    }

    public MarkDocumentPassAsUpdatedEvent(Object documentPrimaryKey, boolean read) {
        super(documentPrimaryKey);
        this.read = read;
    }

    public MarkDocumentPassAsUpdatedEvent(Document document) {
        this(document, true);
    }

    public MarkDocumentPassAsUpdatedEvent(Document document, boolean read) {
        super(document);
        this.read = read;
    }

    public boolean isRead() {
        return read;
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setRead(read);
    }
}
