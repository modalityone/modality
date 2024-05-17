package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.DocumentLine;

/**
 * @author Bruno Salmon
 */
public final class RemoveDocumentLineEvent extends AbstractDocumentLineEvent {

    public RemoveDocumentLineEvent(DocumentLine documentLine) {
        super(documentLine);
    }
}
