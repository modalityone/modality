package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.AbstractMoneyTransferEvent;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractMoneyTransferEventSerialCodec<T extends AbstractMoneyTransferEvent> extends AbstractDocumentEventSerialCodec<T> {

    private static final String MONEY_TRANSFER_PRIMARY_KEY_KEY = "moneyTransfer";
    private static final String AMOUNT_KEY = "amount";
    private static final String PENDING_KEY = "pending";
    private static final String SUCCESSFUL_KEY = "successful";

    public AbstractMoneyTransferEventSerialCodec(Class<T> javaClass, String codecId) {
        super(javaClass, codecId);
    }

    @Override
    public void encode(T o, AstObject serial) {
        super.encode(o, serial);
        encodeObject( serial, MONEY_TRANSFER_PRIMARY_KEY_KEY, o.getMoneyTransferPrimaryKey());
        encodeInteger(serial, AMOUNT_KEY,                     o.getAmount(),    NullEncoding.NULL_VALUE_NOT_ALLOWED);
        encodeBoolean(serial, PENDING_KEY,                    o.isPending(),    NullEncoding.NULL_VALUE_NOT_ALLOWED);
        encodeBoolean(serial, SUCCESSFUL_KEY,                 o.isSuccessful(), NullEncoding.NULL_VALUE_NOT_ALLOWED);
    }

    protected Object decodeMoneyTransferPrimaryKey(ReadOnlyAstObject serial) {
        return decodeObject( serial, MONEY_TRANSFER_PRIMARY_KEY_KEY);
    }

    protected int decodeAmount(ReadOnlyAstObject serial) {
        return decodeInteger(serial, AMOUNT_KEY);
    }

    protected boolean decodePending(ReadOnlyAstObject serial) {
        return decodeBoolean(serial, PENDING_KEY);
    }

    protected boolean decodeSuccessful(ReadOnlyAstObject serial) {
        return decodeBoolean(serial, SUCCESSFUL_KEY);
    }

}
