package one.modality.ecommerce.document.service.events.security;

import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentAsUncheckedEvent extends AbstractSecurityDocumentEvent {

    public MarkDocumentAsUncheckedEvent(Object documentPrimaryKey) {
        super(documentPrimaryKey, false,false,false);
    }

    public MarkDocumentAsUncheckedEvent(Document document) {
        super(document, false,false,false);
    }

}
