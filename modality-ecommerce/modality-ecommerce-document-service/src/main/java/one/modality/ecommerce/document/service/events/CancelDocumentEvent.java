package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public class CancelDocumentEvent extends AbstractDocumentEvent {

    public CancelDocumentEvent(Object documentPrimaryKey) {
        super(documentPrimaryKey);
    }

    public CancelDocumentEvent(Document document) {
        super(document);
    }
}
