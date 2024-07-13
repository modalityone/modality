package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.AbstractMoneyTransferEvent;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractMoneyTransferEventSerialCodec<T extends AbstractMoneyTransferEvent> extends AbstractDocumentEventSerialCodec<T> {

    private static final String MONEY_TRANSFER_PRIMARY_KEY_KEY = "moneyTransfer";

    public AbstractMoneyTransferEventSerialCodec(Class<T> javaClass, String codecId) {
        super(javaClass, codecId);
    }

    @Override
    public void encode(T o, AstObject serial) {
        super.encode(o, serial);
        encodeObject( serial, MONEY_TRANSFER_PRIMARY_KEY_KEY, o.getMoneyTransferPrimaryKey(), NullEncoding.NULL_VALUE_NOT_ALLOWED);
    }

    protected Object decodeMoneyTransferPrimaryKey(ReadOnlyAstObject serial) {
        return decodeObject( serial, MONEY_TRANSFER_PRIMARY_KEY_KEY);
    }

}
