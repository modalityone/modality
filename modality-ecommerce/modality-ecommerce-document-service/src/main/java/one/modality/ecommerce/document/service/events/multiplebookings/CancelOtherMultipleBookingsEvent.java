package one.modality.ecommerce.document.service.events.multiplebookings;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class CancelOtherMultipleBookingsEvent extends AbstractDocumentEvent {

    public CancelOtherMultipleBookingsEvent(Object documentPrimaryKey) {
        super(documentPrimaryKey);
    }

    public CancelOtherMultipleBookingsEvent(Document document) {
        super(document);
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setFieldValue("triggerCancelOtherMultipleBookings", true);
    }
}
