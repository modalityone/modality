package one.modality.ecommerce.document.service.events.security;

import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentAsVerifiedEvent extends AbstractSecurityDocumentEvent {

    public MarkDocumentAsVerifiedEvent(Object documentPrimaryKey) {
        super(documentPrimaryKey, false,true,true);
    }

    public MarkDocumentAsVerifiedEvent(Document document) {
        super(document, false,true,true);
    }

}
