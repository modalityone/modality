package one.modality.ecommerce.document.service.events.multiplebookings;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class GetBackCancelledMultipleBookingsDepositEvent extends AbstractDocumentEvent {

    public GetBackCancelledMultipleBookingsDepositEvent(Object documentPrimaryKey) {
        super(documentPrimaryKey);
    }

    public GetBackCancelledMultipleBookingsDepositEvent(Document document) {
        super(document);
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setFieldValue("triggerTransferFromOtherMultipleBookings", true);
    }
}
