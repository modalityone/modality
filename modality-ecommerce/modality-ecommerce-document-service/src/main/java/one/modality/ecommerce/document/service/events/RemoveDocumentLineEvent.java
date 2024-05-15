package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.DocumentLine;

/**
 * @author Bruno Salmon
 */
public class RemoveDocumentLineEvent extends DocumentLineEvent {

    public RemoveDocumentLineEvent(DocumentLine documentLine) {
        super(documentLine);
    }
}
