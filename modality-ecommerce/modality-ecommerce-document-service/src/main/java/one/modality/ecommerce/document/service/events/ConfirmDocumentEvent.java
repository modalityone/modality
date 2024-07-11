package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public class ConfirmDocumentEvent extends AbstractSetDocumentFieldsEvent {

    private static final Object[] FIELD_IDS = { "confirmed", "read" };

    public ConfirmDocumentEvent(Object documentPrimaryKey, boolean confirmed) {
        this(documentPrimaryKey, confirmed, false);
    }

    public ConfirmDocumentEvent(Object documentPrimaryKey, boolean confirmed, boolean read) {
        super(documentPrimaryKey, FIELD_IDS, confirmed, read);
    }

    public ConfirmDocumentEvent(Document document, boolean confirmed) {
        this(document, confirmed, false);
    }

    public ConfirmDocumentEvent(Document document, boolean confirmed, boolean read) {
        super(document, FIELD_IDS, confirmed, read);
    }

    public boolean isConfirmed() {
        return isConfirmed(getFieldValues());
    }

    public boolean isRead() {
        return isRead(getFieldValues());
    }

    public static boolean isConfirmed(Object[] fieldValues) {
        return (boolean) fieldValues[0];
    }

    public static boolean isRead(Object[] fieldValues) {
        return (boolean) fieldValues[1];
    }
}
