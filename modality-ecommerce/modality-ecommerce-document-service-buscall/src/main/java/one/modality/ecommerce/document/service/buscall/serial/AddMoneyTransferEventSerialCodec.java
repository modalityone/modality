package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.AddMoneyTransferEvent;

/**
 * @author Bruno Salmon
 */
public final class AddMoneyTransferEventSerialCodec extends AbstractMoneyTransferEventSerialCodec<AddMoneyTransferEvent> {

    private static final String CODEC_ID = "AddMoneyTransferEvent";

    public AddMoneyTransferEventSerialCodec() {
        super(AddMoneyTransferEvent.class, CODEC_ID);
    }

    @Override
    public AddMoneyTransferEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new AddMoneyTransferEvent(
                decodeDocumentPrimaryKey(serial),
                decodeMoneyTransferPrimaryKey(serial),
                decodeAmount(serial),
                decodePending(serial),
                decodeSuccessful(serial)
        ), serial);
    }
}
