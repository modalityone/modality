package one.modality.ecommerce.document.service.buscall.serial.gateway;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractExistingMoneyTransferEventSerialCodec;
import one.modality.ecommerce.document.service.events.gateway.UpdateMoneyTransferEvent;

/**
 * @author Bruno Salmon
 */
public final class UpdateMoneyTransferEventSerialCodec extends AbstractExistingMoneyTransferEventSerialCodec<UpdateMoneyTransferEvent> {

    private static final String CODEC_ID = "UpdateMoneyTransferEvent";

    public UpdateMoneyTransferEventSerialCodec() {
        super(UpdateMoneyTransferEvent.class, CODEC_ID);
    }

    @Override
    public UpdateMoneyTransferEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new UpdateMoneyTransferEvent(
                decodeDocumentPrimaryKey(serial),
                decodeMoneyTransferPrimaryKey(serial),
                decodeAmount(serial),
                decodePending(serial),
                decodeSuccessful(serial)
        ), serial);
    }
}
