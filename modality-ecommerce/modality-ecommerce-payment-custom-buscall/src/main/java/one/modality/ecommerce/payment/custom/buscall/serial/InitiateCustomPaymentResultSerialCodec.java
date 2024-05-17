package one.modality.ecommerce.payment.custom.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentResult;

public final class InitiateCustomPaymentResultSerialCodec extends SerialCodecBase<InitiateCustomPaymentResult> {

    private static final String CODEC_ID = "InitiateCustomPaymentResult";
    private static final String HTML_CONTENT_KEY = "htmlContent";

    public InitiateCustomPaymentResultSerialCodec() {
        super(InitiateCustomPaymentResult.class, CODEC_ID);
    }

    @Override
    public void encode(InitiateCustomPaymentResult arg, AstObject serial) {
        encodeString(serial, HTML_CONTENT_KEY, arg.getHtmlContent(), NullEncoding.NULL_VALUE_NOT_ALLOWED);
    }

    @Override
    public InitiateCustomPaymentResult decode(ReadOnlyAstObject serial) {
        return new InitiateCustomPaymentResult(
                decodeString(serial, HTML_CONTENT_KEY, NullEncoding.NULL_VALUE_NOT_ALLOWED)
        );
    }
}
