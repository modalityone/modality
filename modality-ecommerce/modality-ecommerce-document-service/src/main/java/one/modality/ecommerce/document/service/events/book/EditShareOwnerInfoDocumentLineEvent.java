package one.modality.ecommerce.document.service.events.book;

import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class EditShareOwnerInfoDocumentLineEvent extends AbstractDocumentLineEvent {

    private final String[] matesNames;

    public EditShareOwnerInfoDocumentLineEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, String[] matesNames) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.matesNames = matesNames;
    }

    public EditShareOwnerInfoDocumentLineEvent(DocumentLine documentLine, String[] matesNames) {
        super(documentLine);
        this.matesNames = matesNames;
    }

    public String[] getMatesNames() {
        return matesNames;
    }

    @Override
    public void replayEventOnDocumentLine() {
        super.replayEventOnDocumentLine();
        documentLine.setShareMate(false);
        documentLine.setShareOwner(true);
        documentLine.setShareOwnerMatesNames(matesNames);
        documentLine.setShareOwnerQuantity(1 + matesNames.length);
    }
}
