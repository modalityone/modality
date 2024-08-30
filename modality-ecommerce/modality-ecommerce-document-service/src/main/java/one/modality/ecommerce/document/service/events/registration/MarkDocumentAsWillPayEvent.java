package one.modality.ecommerce.document.service.events.registration;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractSetDocumentFieldsEvent;

/**
 * @author Bruno Salmon
 */
public class MarkDocumentAsWillPayEvent extends AbstractSetDocumentFieldsEvent {

    private static final Object[] FIELD_IDS = { "willPay" };

    public MarkDocumentAsWillPayEvent(Object documentPrimaryKey, boolean read) {
        super(documentPrimaryKey, FIELD_IDS, read);
    }

    public MarkDocumentAsWillPayEvent(Document document, boolean read) {
        super(document, FIELD_IDS, read);
    }

    public boolean isWillPay() {
        return isWillPay(getFieldValues());
    }

    public static boolean isWillPay(Object[] fieldValues) {
        return (boolean) fieldValues[0];
    }
}
