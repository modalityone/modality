package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.AddMoneyTransferEvent;

/**
 * @author Bruno Salmon
 */
public final class AddMoneyTransferEventSerialCodec extends AbstractDocumentEventSerialCodec<AddMoneyTransferEvent> {

    private static final String CODEC_ID = "AddMoneyTransferEvent";

    private static final String MONEY_TRANSFER_PRIMARY_KEY_KEY = "moneyTransfer";
    private static final String AMOUNT_KEY = "amount";
    private static final String PENDING_KEY = "pending";
    private static final String SUCCESSFUL_KEY = "successful";

    public AddMoneyTransferEventSerialCodec() {
        super(AddMoneyTransferEvent.class, CODEC_ID);
    }

    @Override
    public void encode(AddMoneyTransferEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeObject( serial, MONEY_TRANSFER_PRIMARY_KEY_KEY, o.getMoneyTransferPrimaryKey());
        encodeInteger(serial, AMOUNT_KEY,                     o.getAmount(),    NullEncoding.NULL_VALUE_NOT_ALLOWED);
        encodeBoolean(serial, PENDING_KEY,                    o.isPending(),    NullEncoding.NULL_VALUE_NOT_ALLOWED);
        encodeBoolean(serial, SUCCESSFUL_KEY,                 o.isSuccessful(), NullEncoding.NULL_VALUE_NOT_ALLOWED);
    }

    @Override
    public AddMoneyTransferEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new AddMoneyTransferEvent(
                decodeDocumentPrimaryKey(serial),
                decodeObject( serial, MONEY_TRANSFER_PRIMARY_KEY_KEY),
                decodeInteger(serial, AMOUNT_KEY),
                decodeBoolean(serial, PENDING_KEY),
                decodeBoolean(serial, SUCCESSFUL_KEY)
        ), serial);
    }
}
