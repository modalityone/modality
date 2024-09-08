package one.modality.ecommerce.document.service.events.registration;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentPassAsReadyEvent extends AbstractDocumentEvent {

    private final boolean passReady;
    private final boolean read;

    public MarkDocumentPassAsReadyEvent(Object documentPrimaryKey, boolean passReady) {
        this(documentPrimaryKey, passReady, true);
    }

    public MarkDocumentPassAsReadyEvent(Object documentPrimaryKey, boolean passReady, boolean read) {
        super(documentPrimaryKey);
        this.passReady = passReady;
        this.read = read;
    }

    public MarkDocumentPassAsReadyEvent(Document document, boolean passReady) {
        this(document, passReady, true);
    }

    public MarkDocumentPassAsReadyEvent(Document document, boolean passReady, boolean read) {
        super(document);
        this.passReady = passReady;
        this.read = read;
    }

    public boolean isPassReady() {
        return passReady;
    }

    public boolean isRead() {
        return read;
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setPassReady(passReady);
        document.setRead(read);
    }
}
