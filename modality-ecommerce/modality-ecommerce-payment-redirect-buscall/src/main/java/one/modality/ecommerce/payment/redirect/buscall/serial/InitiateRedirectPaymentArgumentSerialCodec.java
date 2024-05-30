package one.modality.ecommerce.payment.redirect.buscall.serial;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.AstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentArgument;

public final class InitiateRedirectPaymentArgumentSerialCodec extends SerialCodecBase<InitiateRedirectPaymentArgument> {

    private static final String CODEC_ID = "InitiateRedirectPaymentArgument";
    private static final String DESCRIPTION_KEY = "description";
    private static final String AMOUNT_KEY = "amount";
    private static final String CURRENCY_KEY = "currency";

    public InitiateRedirectPaymentArgumentSerialCodec() {
        super(InitiateRedirectPaymentArgument.class, CODEC_ID);
    }

    @Override
    public void encode(InitiateRedirectPaymentArgument arg, AstObject serial) {
        encodeString( serial, DESCRIPTION_KEY, arg.getDescription());
        encodeInteger(serial, AMOUNT_KEY,      arg.getAmount());
        encodeString( serial, CURRENCY_KEY,    arg.getCurrency());
    }

    @Override
    public InitiateRedirectPaymentArgument decode(ReadOnlyAstObject serial) {
        return new InitiateRedirectPaymentArgument(
                decodeString( serial, DESCRIPTION_KEY),
                decodeInteger(serial, AMOUNT_KEY),
                decodeString( serial, CURRENCY_KEY)
        );
    }
}
