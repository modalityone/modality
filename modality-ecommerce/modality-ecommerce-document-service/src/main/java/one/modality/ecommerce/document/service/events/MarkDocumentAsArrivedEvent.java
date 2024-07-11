package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentAsArrivedEvent extends AbstractSetDocumentFieldsEvent {

    private static final Object[] FIELD_IDS = { "arrived", "read" };

    public MarkDocumentAsArrivedEvent(Object documentPrimaryKey, boolean arrived) {
        this(documentPrimaryKey, arrived, false);
    }

    public MarkDocumentAsArrivedEvent(Object documentPrimaryKey, boolean arrived, boolean read) {
        super(documentPrimaryKey, FIELD_IDS, arrived, read);
    }

    public MarkDocumentAsArrivedEvent(Document document, boolean arrived) {
        this(document, arrived, false);
    }

    public MarkDocumentAsArrivedEvent(Document document, boolean arrived, boolean read) {
        super(document, FIELD_IDS, arrived, read);
    }

    public boolean isArrived() {
        return isArrived(getFieldValues());
    }

    public boolean isRead() {
        return isRead(getFieldValues());
    }

    public static boolean isArrived(Object[] fieldValues) {
        return (boolean) fieldValues[0];
    }

    public static boolean isRead(Object[] fieldValues) {
        return (boolean) fieldValues[1];
    }
}
