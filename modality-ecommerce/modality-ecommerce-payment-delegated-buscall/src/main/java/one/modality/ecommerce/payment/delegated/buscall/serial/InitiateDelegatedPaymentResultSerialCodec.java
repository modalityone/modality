package one.modality.ecommerce.payment.delegated.buscall.serial;

import dev.webfx.platform.ast.json.JsonObject;
import dev.webfx.platform.ast.json.ReadOnlyJsonObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentResult;

public final class InitiateDelegatedPaymentResultSerialCodec extends SerialCodecBase<InitiateDelegatedPaymentResult> {

    private static final String CODEC_ID = "InitiateDelegatedPaymentResult";
    private static final String DELEGATED_PAYMENT_URL_KEY = "url";

    public InitiateDelegatedPaymentResultSerialCodec() {
        super(InitiateDelegatedPaymentResult.class, CODEC_ID);
    }

    @Override
    public void encodeToJson(InitiateDelegatedPaymentResult arg, JsonObject json) {
        json.set(DELEGATED_PAYMENT_URL_KEY, arg.getDelegatedPaymentUrl());
    }

    @Override
    public InitiateDelegatedPaymentResult decodeFromJson(ReadOnlyJsonObject json) {
        return new InitiateDelegatedPaymentResult(
                json.getString(DELEGATED_PAYMENT_URL_KEY)
        );
    }
}
