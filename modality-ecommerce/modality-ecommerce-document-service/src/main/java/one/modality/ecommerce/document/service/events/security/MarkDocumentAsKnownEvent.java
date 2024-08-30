package one.modality.ecommerce.document.service.events.security;

import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentAsKnownEvent extends AbstractSecurityDocumentEvent {

    public MarkDocumentAsKnownEvent(Object documentPrimaryKey) {
        super(documentPrimaryKey, false,true,false);
    }

    public MarkDocumentAsKnownEvent(Document document) {
        super(document, false,true,false);
    }

}
