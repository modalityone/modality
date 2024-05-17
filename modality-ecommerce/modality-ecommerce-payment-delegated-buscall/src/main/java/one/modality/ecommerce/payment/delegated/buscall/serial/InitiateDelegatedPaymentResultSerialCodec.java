package one.modality.ecommerce.payment.delegated.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentResult;

public final class InitiateDelegatedPaymentResultSerialCodec extends SerialCodecBase<InitiateDelegatedPaymentResult> {

    private static final String CODEC_ID = "InitiateDelegatedPaymentResult";
    private static final String DELEGATED_PAYMENT_URL_KEY = "url";

    public InitiateDelegatedPaymentResultSerialCodec() {
        super(InitiateDelegatedPaymentResult.class, CODEC_ID);
    }

    @Override
    public void encode(InitiateDelegatedPaymentResult arg, AstObject serial) {
        encodeString(serial, DELEGATED_PAYMENT_URL_KEY, arg.getDelegatedPaymentUrl(), NullEncoding.NULL_VALUE_NOT_ALLOWED);
    }

    @Override
    public InitiateDelegatedPaymentResult decode(ReadOnlyAstObject serial) {
        return new InitiateDelegatedPaymentResult(
                decodeString(serial, DELEGATED_PAYMENT_URL_KEY, NullEncoding.NULL_VALUE_NOT_ALLOWED)
        );
    }
}
