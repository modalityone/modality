package one.modality.ecommerce.document.service.events.multiplebookings;

import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class MergeMultipleBookingsOptionsEvent extends AbstractDocumentEvent {

    public MergeMultipleBookingsOptionsEvent(Object documentPrimaryKey) {
        super(documentPrimaryKey);
    }

    public MergeMultipleBookingsOptionsEvent(Document document) {
        super(document);
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setFieldValue("triggerMergeFromOtherMultipleBookings", true);
    }
}
