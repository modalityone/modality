package one.modality.ecommerce.document.service.events.multiplebookings;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public class MarkNotMultipleBookingEvent extends AbstractDocumentEvent {

    private final Object notMultipleBookingPrimaryKey;

    public MarkNotMultipleBookingEvent(Object documentPrimaryKey, Object notMultipleBookingPrimaryKey) {
        super(documentPrimaryKey);
        this.notMultipleBookingPrimaryKey = notMultipleBookingPrimaryKey;
    }

    public MarkNotMultipleBookingEvent(Document document, Document notMultipleBooking) {
        super(document);
        this.notMultipleBookingPrimaryKey = Entities.getPrimaryKey(notMultipleBooking);
    }

    public Object getNotMultipleBookingPrimaryKey() {
        return notMultipleBookingPrimaryKey;
    }

    @Override
    public void replayEventOnDocument() {
        super.replayEventOnDocument();
        document.setFieldValue("notMultipleBookingPrimaryKey", notMultipleBookingPrimaryKey);
    }
}
