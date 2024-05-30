package one.modality.ecommerce.payment.embedded.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentResult;

public final class InitiateEmbeddedPaymentResultSerialCodec extends SerialCodecBase<InitiateEmbeddedPaymentResult> {

    private static final String CODEC_ID = "InitiateEmbeddedPaymentResult";
    private static final String HTML_CONTENT_KEY = "htmlContent";

    public InitiateEmbeddedPaymentResultSerialCodec() {
        super(InitiateEmbeddedPaymentResult.class, CODEC_ID);
    }

    @Override
    public void encode(InitiateEmbeddedPaymentResult arg, AstObject serial) {
        encodeString(serial, HTML_CONTENT_KEY, arg.getHtmlContent(), NullEncoding.NULL_VALUE_NOT_ALLOWED);
    }

    @Override
    public InitiateEmbeddedPaymentResult decode(ReadOnlyAstObject serial) {
        return new InitiateEmbeddedPaymentResult(
                decodeString(serial, HTML_CONTENT_KEY, NullEncoding.NULL_VALUE_NOT_ALLOWED)
        );
    }
}
