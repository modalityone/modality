package one.modality.ecommerce.document.service.events.registration.documentline;

import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class EditShareMateInfoDocumentLineEvent extends AbstractDocumentLineEvent {

    private final String ownerName;

    public EditShareMateInfoDocumentLineEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, String ownerName) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.ownerName = ownerName;
    }

    public EditShareMateInfoDocumentLineEvent(DocumentLine documentLine, String ownerName) {
        super(documentLine);
        this.ownerName = ownerName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public void replayEventOnDocumentLine() {
        super.replayEventOnDocumentLine();
        documentLine.setShareMate(true);
        documentLine.setShareOwner(false);
        if (ownerName != null)
            documentLine.setShareMateOwnerName(ownerName);
    }
}
