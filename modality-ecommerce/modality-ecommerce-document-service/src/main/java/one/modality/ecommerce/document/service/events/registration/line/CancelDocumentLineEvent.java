package one.modality.ecommerce.document.service.events.registration.line;

import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.events.AbstractSetDocumentLineFieldsEvent;

/**
 * @author Bruno Salmon
 */
public final class CancelDocumentLineEvent extends AbstractSetDocumentLineFieldsEvent {

    private static final Object[] FIELD_IDS = { "cancelled", "read" };

    public CancelDocumentLineEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, boolean cancelled, boolean read) {
        super(documentPrimaryKey, documentLinePrimaryKey, FIELD_IDS, cancelled, read);
    }

    public CancelDocumentLineEvent(DocumentLine documentLine, boolean cancelled, boolean read) {
        super(documentLine, FIELD_IDS, cancelled, read);
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
