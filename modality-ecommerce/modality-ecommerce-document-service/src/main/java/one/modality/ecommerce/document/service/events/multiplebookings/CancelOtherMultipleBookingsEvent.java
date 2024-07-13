package one.modality.ecommerce.document.service.events.multiplebookings;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractSetDocumentFieldsEvent;

/**
 * @author Bruno Salmon
 */
public class CancelOtherMultipleBookingsEvent extends AbstractSetDocumentFieldsEvent {

    private static final Object[] FIELD_IDS = { "triggerCancelOtherMultipleBookings" };

    public CancelOtherMultipleBookingsEvent(Object documentPrimaryKey) {
        super(documentPrimaryKey, FIELD_IDS, true);
    }

    public CancelOtherMultipleBookingsEvent(Document document) {
        super(document, FIELD_IDS, true);
    }

}