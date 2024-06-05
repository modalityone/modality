package one.modality.ecommerce.payment.buscall.serial;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.AstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.MakeApiPaymentArgument;

/**************************************
 *           Serial Codec             *
 * ***********************************/

public final class MakeApiPaymentArgumentSerialCodec extends SerialCodecBase<MakeApiPaymentArgument> {

    private static final String CODEC_ID = "MakeApiPaymentArgument";
    private static final String AMOUNT_KEY = "amount";
    private static final String DOCUMENT_PRIMARY_KEY_KEY = "document";
    private static final String CC_NUMBER_KEY = "ccNumber";
    private static final String CC_EXPIRY_KEY = "ccExpiry";

    public MakeApiPaymentArgumentSerialCodec() {
        super(MakeApiPaymentArgument.class, CODEC_ID);
    }

    @Override
    public void encode(MakeApiPaymentArgument arg, AstObject serial) {
        encodeInteger(serial, AMOUNT_KEY,               arg.getAmount());
        encodeObject( serial, DOCUMENT_PRIMARY_KEY_KEY, arg.getDocumentPrimaryKey());
        encodeString( serial, CC_NUMBER_KEY,            arg.getCcNumber());
        encodeString( serial, CC_EXPIRY_KEY,            arg.getCcExpiry());
    }

    @Override
    public MakeApiPaymentArgument decode(ReadOnlyAstObject serial) {
        return new MakeApiPaymentArgument(
                decodeInteger(serial, AMOUNT_KEY),
                decodeObject( serial, DOCUMENT_PRIMARY_KEY_KEY),
                decodeString( serial, CC_NUMBER_KEY),
                decodeString( serial, CC_EXPIRY_KEY)
        );
    }
}
