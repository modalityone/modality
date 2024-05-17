package one.modality.ecommerce.payment.direct.buscall.serial;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.AstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentArgument;

/**************************************
 *           Serial Codec             *
 * ***********************************/

public final class MakeDirectPaymentArgumentSerialCodec extends SerialCodecBase<MakeDirectPaymentArgument> {

    private static final String CODEC_ID = "DirectPaymentArgument";
    private static final String AMOUNT_KEY = "amount";
    private static final String CURRENCY_KEY = "currency";
    private static final String CC_NUMBER_KEY = "ccNumber";
    private static final String CC_EXPIRY_KEY = "ccExpiry";

    public MakeDirectPaymentArgumentSerialCodec() {
        super(MakeDirectPaymentArgument.class, CODEC_ID);
    }

    @Override
    public void encode(MakeDirectPaymentArgument arg, AstObject serial) {
        encodeInteger(serial, AMOUNT_KEY,    arg.getAmount());
        encodeString( serial, CURRENCY_KEY,  arg.getCurrency());
        encodeString( serial, CC_NUMBER_KEY, arg.getCcNumber());
        encodeString( serial, CC_EXPIRY_KEY, arg.getCcExpiry());
    }

    @Override
    public MakeDirectPaymentArgument decode(ReadOnlyAstObject serial) {
        return new MakeDirectPaymentArgument(
                decodeInteger(serial, AMOUNT_KEY),
                decodeString( serial, CURRENCY_KEY),
                decodeString( serial, CC_NUMBER_KEY),
                decodeString( serial, CC_EXPIRY_KEY)
        );
    }
}
