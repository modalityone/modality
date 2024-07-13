package one.modality.ecommerce.document.service.buscall.serial.registration.moneytransfer;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractMoneyTransferEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.moneytransfer.RemoveMoneyTransferEvent;

/**
 * @author Bruno Salmon
 */
public final class RemoveMoneyTransferEventSerialCodec extends AbstractMoneyTransferEventSerialCodec<RemoveMoneyTransferEvent> {

    private static final String CODEC_ID = "RemoveMoneyTransferEvent";

    public RemoveMoneyTransferEventSerialCodec() {
        super(RemoveMoneyTransferEvent.class, CODEC_ID);
    }

    @Override
    public RemoveMoneyTransferEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new RemoveMoneyTransferEvent(
                decodeDocumentPrimaryKey(serial),
                decodeMoneyTransferPrimaryKey(serial)
        ), serial);
    }
}
