package one.modality.ecommerce.document.service.buscall.serial.multiplebookings;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.multiplebookings.MarkNotMultipleBookingEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkNotMultipleBookingEventSerialCodec extends AbstractDocumentEventSerialCodec<MarkNotMultipleBookingEvent> {

    private static final String CODEC_ID = "MarkNotMultipleBookingEvent";

    private static final String NOT_MULTIPLE_BOOKING_PRIMARY_KEY_KEY = "notMultipleBooking";

    public MarkNotMultipleBookingEventSerialCodec() {
        super(MarkNotMultipleBookingEvent.class, CODEC_ID);
    }

    @Override
    public void encode(MarkNotMultipleBookingEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeObject(serial, NOT_MULTIPLE_BOOKING_PRIMARY_KEY_KEY, o.getNotMultipleBookingPrimaryKey());
    }

    @Override
    public MarkNotMultipleBookingEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new MarkNotMultipleBookingEvent(
                decodeDocumentPrimaryKey(serial),
                decodeObject(serial, NOT_MULTIPLE_BOOKING_PRIMARY_KEY_KEY)
        ), serial);
    }
}
