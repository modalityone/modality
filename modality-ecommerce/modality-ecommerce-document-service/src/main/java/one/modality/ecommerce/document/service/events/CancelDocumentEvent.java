package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public class CancelDocumentEvent extends AbstractSetDocumentFieldsEvent {

    private static final Object[] FIELD_IDS = { "cancelled", "read" };

    public CancelDocumentEvent(Object documentPrimaryKey, boolean cancelled) {
        this(documentPrimaryKey, cancelled, false);
    }

    public CancelDocumentEvent(Object documentPrimaryKey, boolean cancelled, boolean read) {
        super(documentPrimaryKey, FIELD_IDS, cancelled, read);
    }

    public CancelDocumentEvent(Document document, boolean cancelled) {
        this(document, cancelled, false);
    }

    public CancelDocumentEvent(Document document, boolean cancelled, boolean read) {
        super(document, FIELD_IDS, cancelled, read);
    }

    public boolean isCancelled() {
        return isCancelled(getFieldValues());
    }

    public boolean isRead() {
        return isRead(getFieldValues());
    }

    public static boolean isCancelled(Object[] fieldValues) {
        return (boolean) fieldValues[0];
    }

    public static boolean isRead(Object[] fieldValues) {
        return (boolean) fieldValues[1];
    }
}
