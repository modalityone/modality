package one.modality.ecommerce.document.service.buscall.serial.multiplebookings;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractSetDocumentFieldsEventSerialCodec;
import one.modality.ecommerce.document.service.events.multiplebookings.MergeMultipleBookingsOptionsEvent;

/**
 * @author Bruno Salmon
 */
public final class MergeMultipleBookingsOptionsEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<MergeMultipleBookingsOptionsEvent> {

    private static final String CODEC_ID = "MergeMultipleBookingsOptionsEvent";

    public MergeMultipleBookingsOptionsEventSerialCodec() {
        super(MergeMultipleBookingsOptionsEvent.class, CODEC_ID);
    }

    @Override
    public MergeMultipleBookingsOptionsEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new MergeMultipleBookingsOptionsEvent(
                decodeDocumentPrimaryKey(serial)
        ), serial);
    }
}
