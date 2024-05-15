package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.DocumentLine;

/**
 * @author Bruno Salmon
 */
public final class UncancelDocumentLineEvent extends EditDocumentLineEvent {

    public UncancelDocumentLineEvent(DocumentLine documentLine) {
        super(documentLine);
    }
}
