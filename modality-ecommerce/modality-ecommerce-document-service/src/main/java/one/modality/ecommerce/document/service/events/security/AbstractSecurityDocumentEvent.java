package one.modality.ecommerce.document.service.events.security;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractSetDocumentFieldsEvent;

/**
 * @author Bruno Salmon
 */
public class AbstractSecurityDocumentEvent extends AbstractSetDocumentFieldsEvent {

    private static final Object[] FIELD_IDS = { "person_unknown", "person_known", "person_verified" };

    public AbstractSecurityDocumentEvent(Object documentPrimaryKey, boolean person_unknown, boolean person_known, boolean person_verified) {
        super(documentPrimaryKey, FIELD_IDS, person_unknown, person_known, person_verified);
    }

    public AbstractSecurityDocumentEvent(Document document, boolean person_unknown, boolean person_known, boolean person_verified) {
        super(document, FIELD_IDS, person_unknown, person_known, person_verified);
    }
}
