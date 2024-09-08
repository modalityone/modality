package one.modality.ecommerce.document.service.events.security;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentAsUnknownEvent extends AbstractDocumentEvent {

    public MarkDocumentAsUnknownEvent(Object documentPrimaryKey) {
        super(documentPrimaryKey);
    }

    public MarkDocumentAsUnknownEvent(Document document) {
        super(document);
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setFieldValue("person_unknown", true);
        document.setFieldValue("person_known", false);
        document.setFieldValue("person_verified", false);
    }
}
