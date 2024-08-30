package one.modality.ecommerce.document.service.events.registration;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractSetDocumentFieldsEvent;

/**
 * @author Bruno Salmon
 */
public class FlagDocumentEvent extends AbstractSetDocumentFieldsEvent {

    private static final Object[] FIELD_IDS = { "flagged" };

    public FlagDocumentEvent(Object documentPrimaryKey, boolean flagged) {
        super(documentPrimaryKey, FIELD_IDS, flagged);
    }

    public FlagDocumentEvent(Document document, boolean flagged) {
        super(document, FIELD_IDS, flagged);
    }

    public boolean isFlagged() {
        return isFlagged(getFieldValues());
    }

    public static boolean isFlagged(Object[] fieldValues) {
        return (boolean) fieldValues[0];
    }
}
