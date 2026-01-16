package one.modality.ecommerce.document.service.events.registration.documentline;

import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Person;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class LinkMateToOwnerDocumentLineEvent extends AbstractDocumentLineEvent {

    private final Object ownerDocumentLine;
    private final Object ownerPerson; // alternatively if `ownerDocumentLine` is null (when the owner hasn't booked yet)

    public LinkMateToOwnerDocumentLineEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, Object ownerDocumentLinePrimaryKey, Object ownerPersonPrimaryKey) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.ownerPerson = ownerPersonPrimaryKey;
        this.ownerDocumentLine = ownerDocumentLinePrimaryKey;
    }

    public LinkMateToOwnerDocumentLineEvent(DocumentLine documentLine, DocumentLine ownerDocumentLine) {
        this(documentLine, ownerDocumentLine, null);
    }

    public LinkMateToOwnerDocumentLineEvent(DocumentLine documentLine, Person ownerPerson) {
        this(documentLine, null, ownerPerson);
    }

    public LinkMateToOwnerDocumentLineEvent(DocumentLine documentLine, DocumentLine ownerDocumentLine, Person ownerPerson) {
        super(documentLine);
        this.ownerPerson = ownerPerson;
        this.ownerDocumentLine = ownerDocumentLine;
    }

    public Object getOwnerDocumentLine() {
        return ownerDocumentLine;
    }

    public Object getOwnerPerson() {
        return ownerPerson;
    }

    @Override
    public void replayEventOnDocumentLine() {
        super.replayEventOnDocumentLine();
        if (ownerDocumentLine != null)
            documentLine.setShareMateOwnerDocumentLine(ownerDocumentLine);
        if (ownerPerson != null)
            documentLine.setShareMateOwnerPerson(ownerPerson);
    }
}
