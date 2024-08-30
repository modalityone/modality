package one.modality.ecommerce.document.service.buscall.serial.multiplebookings;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractSetDocumentFieldsEventSerialCodec;
import one.modality.ecommerce.document.service.events.multiplebookings.MarkNotMultipleBookingEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkNotMultipleBookingEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<MarkNotMultipleBookingEvent> {

    private static final String CODEC_ID = "MarkNotMultipleBookingEvent";

    public MarkNotMultipleBookingEventSerialCodec() {
        super(MarkNotMultipleBookingEvent.class, CODEC_ID);
    }

    @Override
    public MarkNotMultipleBookingEvent decode(ReadOnlyAstObject serial) {
        Object[] values = decodeValues(serial);
        return postDecode(new MarkNotMultipleBookingEvent(
                decodeDocumentPrimaryKey(serial),
                MarkNotMultipleBookingEvent.getNotMultipleBooking(values)
        ), serial);
    }
}
