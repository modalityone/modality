package one.modality.ecommerce.document.service.events.registration;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractSetDocumentFieldsEvent;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentPassAsReadyEvent extends AbstractSetDocumentFieldsEvent {

    private static final Object[] FIELD_IDS = { "passReady", "read" };

    public MarkDocumentPassAsReadyEvent(Object documentPrimaryKey, boolean passReady) {
        this(documentPrimaryKey, passReady, true);
    }

    public MarkDocumentPassAsReadyEvent(Object documentPrimaryKey, boolean passReady, boolean read) {
        super(documentPrimaryKey, FIELD_IDS, passReady, read);
    }

    public MarkDocumentPassAsReadyEvent(Document document, boolean passReady) {
        this(document, passReady, true);
    }

    public MarkDocumentPassAsReadyEvent(Document document, boolean passReady, boolean read) {
        super(document, FIELD_IDS, passReady, read);
    }

    public boolean isPassReady() {
        return isPassReady(getFieldValues());
    }

    public boolean isRead() {
        return isRead(getFieldValues());
    }

    public static boolean isPassReady(Object[] fieldValues) {
        return (boolean) fieldValues[0];
    }

    public static boolean isRead(Object[] fieldValues) {
        return (boolean) fieldValues[1];
    }
}
