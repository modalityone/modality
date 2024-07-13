package one.modality.ecommerce.document.service.events.registration.documentline;

import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class RemoveDocumentLineEvent extends AbstractDocumentLineEvent {

    public RemoveDocumentLineEvent(Object documentPrimaryKey, Object documentLinePrimaryKey) {
        super(documentPrimaryKey, documentLinePrimaryKey);
    }

    public RemoveDocumentLineEvent(DocumentLine documentLine) {
        super(documentLine);
    }
}
