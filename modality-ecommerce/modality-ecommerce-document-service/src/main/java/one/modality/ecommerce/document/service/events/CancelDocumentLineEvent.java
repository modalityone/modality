package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.DocumentLine;

/**
 * @author Bruno Salmon
 */
public final class CancelDocumentLineEvent extends EditDocumentLineEvent {

    public CancelDocumentLineEvent(DocumentLine documentLine) {
        super(documentLine);
    }

}
