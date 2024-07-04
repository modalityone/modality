package one.modality.ecommerce.payment.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.InitiatePaymentArgument;

public final class InitiatePaymentArgumentSerialCodec extends SerialCodecBase<InitiatePaymentArgument> {

    private static final String CODEC_ID = "InitiatePaymentArgument";
    private static final String AMOUNT_KEY = "amount";
    private static final String DOCUMENT_PRIMARY_KEY_KEY = "document";
    private static final String SEAMLESS_KEY = "seamless";
    private static final String HTTPS_KEY = "https";

    public InitiatePaymentArgumentSerialCodec() {
        super(InitiatePaymentArgument.class, CODEC_ID);
    }

    @Override
    public void encode(InitiatePaymentArgument arg, AstObject serial) {
        encodeInteger(serial, AMOUNT_KEY,               arg.getAmount());
        encodeObject( serial, DOCUMENT_PRIMARY_KEY_KEY, arg.getDocumentPrimaryKey());
        encodeBoolean(serial, SEAMLESS_KEY,             arg.isSeamlessIfSupported());
        encodeBoolean(serial, HTTPS_KEY,                arg.isParentPageHttps());
    }

    @Override
    public InitiatePaymentArgument decode(ReadOnlyAstObject serial) {
        return new InitiatePaymentArgument(
                decodeInteger( serial, AMOUNT_KEY),
                decodeObject(  serial, DOCUMENT_PRIMARY_KEY_KEY),
                decodeBoolean( serial, SEAMLESS_KEY),
                decodeBoolean( serial, HTTPS_KEY)
        );
    }
}
