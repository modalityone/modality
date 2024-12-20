package one.modality.ecommerce.document.service.buscall.serial.multiplebookings;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.multiplebookings.CancelOtherMultipleBookingsEvent;

/**
 * @author Bruno Salmon
 */
public final class CancelOtherMultipleBookingsEventSerialCodec extends AbstractDocumentEventSerialCodec<CancelOtherMultipleBookingsEvent> {

    private static final String CODEC_ID = "CancelOtherMultipleBookingsEvent";

    public CancelOtherMultipleBookingsEventSerialCodec() {
        super(CancelOtherMultipleBookingsEvent.class, CODEC_ID);
    }

    @Override
    public CancelOtherMultipleBookingsEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new CancelOtherMultipleBookingsEvent(
                decodeDocumentPrimaryKey(serial)
        ), serial);
    }
}
