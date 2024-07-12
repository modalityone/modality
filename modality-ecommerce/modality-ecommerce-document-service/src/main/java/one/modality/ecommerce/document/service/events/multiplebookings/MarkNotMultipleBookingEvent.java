package one.modality.ecommerce.document.service.events.multiplebookings;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractSetDocumentFieldsEvent;

/**
 * @author Bruno Salmon
 */
public class MarkNotMultipleBookingEvent extends AbstractSetDocumentFieldsEvent {

    private static final Object[] FIELD_IDS = { "notMultipleBooking" };

    public MarkNotMultipleBookingEvent(Object documentPrimaryKey, Object notMultipleBookingPrimaryKey) {
        super(documentPrimaryKey, FIELD_IDS, notMultipleBookingPrimaryKey);
    }

    public MarkNotMultipleBookingEvent(Document document, Document notMultipleBooking) {
        super(document, FIELD_IDS, notMultipleBooking);
    }

    public Object getNotMultipleBooking() {
        return getNotMultipleBooking(getFieldValues());
    }

    public static Object getNotMultipleBooking(Object[] fieldValues) {
        return fieldValues[0];
    }


}
