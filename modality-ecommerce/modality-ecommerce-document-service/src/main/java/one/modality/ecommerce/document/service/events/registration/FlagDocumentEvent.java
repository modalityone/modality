package one.modality.ecommerce.document.service.events.registration;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class FlagDocumentEvent extends AbstractDocumentEvent {

    private final boolean flagged;

    public FlagDocumentEvent(Object documentPrimaryKey, boolean flagged) {
        super(documentPrimaryKey);
        this.flagged = flagged;
    }

    public FlagDocumentEvent(Document document, boolean flagged) {
        super(document);
        this.flagged = flagged;
    }

    public boolean isFlagged() {
        return flagged;
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setFlagged(flagged);
    }
}
