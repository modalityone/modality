package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentPassAsUpdatedEvent extends AbstractSetDocumentFieldsEvent {

    private static final Object[] FIELD_IDS = { "read" };

    public MarkDocumentPassAsUpdatedEvent(Object documentPrimaryKey) {
        this(documentPrimaryKey, true);
    }

    public MarkDocumentPassAsUpdatedEvent(Object documentPrimaryKey, boolean read) {
        super(documentPrimaryKey, FIELD_IDS, read);
    }

    public MarkDocumentPassAsUpdatedEvent(Document document) {
        this(document, true);
    }

    public MarkDocumentPassAsUpdatedEvent(Document document, boolean read) {
        super(document, FIELD_IDS, read);
    }

    public boolean isRead() {
        return isRead(getFieldValues());
    }

    public static boolean isRead(Object[] fieldValues) {
        return (boolean) fieldValues[0];
    }
}
