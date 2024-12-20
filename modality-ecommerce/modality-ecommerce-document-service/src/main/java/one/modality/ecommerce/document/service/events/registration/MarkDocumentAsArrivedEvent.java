package one.modality.ecommerce.document.service.events.registration;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentAsArrivedEvent extends AbstractDocumentEvent {

    private final boolean arrived;
    private final boolean read;

    public MarkDocumentAsArrivedEvent(Object documentPrimaryKey, boolean arrived) {
        this(documentPrimaryKey, arrived, false);
    }

    public MarkDocumentAsArrivedEvent(Object documentPrimaryKey, boolean arrived, boolean read) {
        super(documentPrimaryKey);
        this.arrived = arrived;
        this.read = read;
    }

    public MarkDocumentAsArrivedEvent(Document document, boolean arrived) {
        this(document, arrived, false);
    }

    public MarkDocumentAsArrivedEvent(Document document, boolean arrived, boolean read) {
        super(document);
        this.arrived = arrived;
        this.read = read;
    }

    public boolean isArrived() {
        return arrived;
    }

    public boolean isRead() {
        return read;
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setArrived(arrived);
        document.setRead(read);
    }
}
