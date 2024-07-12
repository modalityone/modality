package one.modality.ecommerce.document.service.buscall.serial.multiplebookings;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractSetDocumentFieldsEventSerialCodec;
import one.modality.ecommerce.document.service.events.multiplebookings.GetBackCancelledMultipleBookingsDepositEvent;

/**
 * @author Bruno Salmon
 */
public final class GetBackCancelledMultipleBookingsDepositEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<GetBackCancelledMultipleBookingsDepositEvent> {

    private static final String CODEC_ID = "GetBackCancelledMultipleBookingsDepositEvent";

    public GetBackCancelledMultipleBookingsDepositEventSerialCodec() {
        super(GetBackCancelledMultipleBookingsDepositEvent.class, CODEC_ID);
    }

    @Override
    public GetBackCancelledMultipleBookingsDepositEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new GetBackCancelledMultipleBookingsDepositEvent(
                decodeDocumentPrimaryKey(serial)
        ), serial);
    }
}
