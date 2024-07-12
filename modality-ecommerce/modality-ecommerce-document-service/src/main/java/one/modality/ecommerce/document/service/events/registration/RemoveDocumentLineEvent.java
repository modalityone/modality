package one.modality.ecommerce.document.service.events.registration;

import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class RemoveDocumentLineEvent extends AbstractDocumentLineEvent {

    public RemoveDocumentLineEvent(DocumentLine documentLine) {
        super(documentLine);
    }
}
