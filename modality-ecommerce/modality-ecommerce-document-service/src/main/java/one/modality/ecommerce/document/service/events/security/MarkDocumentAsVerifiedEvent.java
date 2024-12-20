package one.modality.ecommerce.document.service.events.security;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentAsVerifiedEvent extends AbstractDocumentEvent {

    public MarkDocumentAsVerifiedEvent(Object documentPrimaryKey) {
        super(documentPrimaryKey);
    }

    public MarkDocumentAsVerifiedEvent(Document document) {
        super(document);
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setFieldValue("person_unknown", false);
        document.setFieldValue("person_known", true);
        document.setFieldValue("person_verified", true);
    }
}
