package one.modality.ecommerce.payment.delegated.buscall.serial;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.AstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentArgument;

public final class InitiateDelegatedPaymentArgumentSerialCodec extends SerialCodecBase<InitiateDelegatedPaymentArgument> {

    private static final String CODEC_ID = "InitiateDelegatedPaymentArgument";
    private static final String DESCRIPTION_KEY = "description";
    private static final String AMOUNT_KEY = "amount";
    private static final String CURRENCY_KEY = "currency";

    public InitiateDelegatedPaymentArgumentSerialCodec() {
        super(InitiateDelegatedPaymentArgument.class, CODEC_ID);
    }

    @Override
    public void encode(InitiateDelegatedPaymentArgument arg, AstObject serial) {
        encodeString( serial, DESCRIPTION_KEY, arg.getDescription());
        encodeInteger(serial, AMOUNT_KEY,      arg.getAmount());
        encodeString( serial, CURRENCY_KEY,    arg.getCurrency());
    }

    @Override
    public InitiateDelegatedPaymentArgument decode(ReadOnlyAstObject serial) {
        return new InitiateDelegatedPaymentArgument(
                decodeString( serial, DESCRIPTION_KEY),
                decodeInteger(serial, AMOUNT_KEY),
                decodeString( serial, CURRENCY_KEY)
        );
    }
}
