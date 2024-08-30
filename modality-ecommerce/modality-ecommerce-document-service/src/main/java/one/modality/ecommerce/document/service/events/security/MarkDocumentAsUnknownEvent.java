package one.modality.ecommerce.document.service.events.security;

import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentAsUnknownEvent extends AbstractSecurityDocumentEvent {

    public MarkDocumentAsUnknownEvent(Object documentPrimaryKey) {
        super(documentPrimaryKey, true,false,false);
    }

    public MarkDocumentAsUnknownEvent(Document document) {
        super(document, true,false,false);
    }

}
