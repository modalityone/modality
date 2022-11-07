package one.modality.ecommerce.payment.custom.buscall.serial;

import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.WritableJsonObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentResult;

public final class InitiateCustomPaymentResultSerialCodec extends SerialCodecBase<InitiateCustomPaymentResult> {

    private static final String CODEC_ID = "InitiateDelegatedPaymentResult";
    private static final String HTML_CONTENT_KEY = "htmlContent";

    public InitiateCustomPaymentResultSerialCodec() {
        super(InitiateCustomPaymentResult.class, CODEC_ID);
    }

    @Override
    public void encodeToJson(InitiateCustomPaymentResult arg, WritableJsonObject json) {
        json.set(HTML_CONTENT_KEY, arg.getHtmlContent());
    }

    @Override
    public InitiateCustomPaymentResult decodeFromJson(JsonObject json) {
        return new InitiateCustomPaymentResult(
                json.getString(HTML_CONTENT_KEY)
        );
    }
}
