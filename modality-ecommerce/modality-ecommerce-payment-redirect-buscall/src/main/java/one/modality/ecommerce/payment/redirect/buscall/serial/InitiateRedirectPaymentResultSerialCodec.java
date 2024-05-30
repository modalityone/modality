package one.modality.ecommerce.payment.redirect.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentResult;

public final class InitiateRedirectPaymentResultSerialCodec extends SerialCodecBase<InitiateRedirectPaymentResult> {

    private static final String CODEC_ID = "InitiateRedirectPaymentResult";
    private static final String REDIRECT_PAYMENT_URL_KEY = "url";

    public InitiateRedirectPaymentResultSerialCodec() {
        super(InitiateRedirectPaymentResult.class, CODEC_ID);
    }

    @Override
    public void encode(InitiateRedirectPaymentResult arg, AstObject serial) {
        encodeString(serial, REDIRECT_PAYMENT_URL_KEY, arg.getRedirectPaymentUrl(), NullEncoding.NULL_VALUE_NOT_ALLOWED);
    }

    @Override
    public InitiateRedirectPaymentResult decode(ReadOnlyAstObject serial) {
        return new InitiateRedirectPaymentResult(
                decodeString(serial, REDIRECT_PAYMENT_URL_KEY, NullEncoding.NULL_VALUE_NOT_ALLOWED)
        );
    }
}
