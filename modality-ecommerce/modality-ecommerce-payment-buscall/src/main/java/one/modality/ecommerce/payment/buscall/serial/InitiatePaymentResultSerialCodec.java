package one.modality.ecommerce.payment.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.InitiatePaymentResult;

public final class InitiatePaymentResultSerialCodec extends SerialCodecBase<InitiatePaymentResult> {

    private static final String CODEC_ID = "InitiatePaymentResult";
    private static final String PAYMENT_PRIMARY_KEY_KEY = "payment";
    private static final String HTML_CONTENT_KEY = "htmlContent";
    private static final String URL_KEY = "url";
    private static final String REDIRECT_KEY = "redirect";

    public InitiatePaymentResultSerialCodec() {
        super(InitiatePaymentResult.class, CODEC_ID);
    }

    @Override
    public void encode(InitiatePaymentResult arg, AstObject serial) {
        encodeObject( serial, PAYMENT_PRIMARY_KEY_KEY, arg.getPaymentPrimaryKey());
        encodeString( serial, HTML_CONTENT_KEY,        arg.getHtmlContent());
        encodeString( serial, URL_KEY,                 arg.getUrl());
        encodeBoolean(serial, REDIRECT_KEY,            arg.isRedirect());
    }

    @Override
    public InitiatePaymentResult decode(ReadOnlyAstObject serial) {
        return new InitiatePaymentResult(
                decodeObject( serial, PAYMENT_PRIMARY_KEY_KEY),
                decodeString( serial, HTML_CONTENT_KEY),
                decodeString( serial, URL_KEY),
                decodeBoolean(serial, REDIRECT_KEY)
        );
    }
}
