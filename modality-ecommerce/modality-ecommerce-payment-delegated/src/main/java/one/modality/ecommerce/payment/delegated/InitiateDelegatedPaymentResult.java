package one.modality.ecommerce.payment.delegated;

import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.WritableJsonObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;

/**
 * @author Bruno Salmon
 */
public class InitiateDelegatedPaymentResult {

    private final String delegatedPaymentUrl;

    public InitiateDelegatedPaymentResult(String delegatedPaymentUrl) {
        this.delegatedPaymentUrl = delegatedPaymentUrl;
    }

    public String getDelegatedPaymentUrl() {
        return delegatedPaymentUrl;
    }

    /**************************************
     *           Serial Codec             *
     * ***********************************/

    private static final String CODEC_ID = "InitiateDelegatedPaymentResult";
    private static final String DELEGATED_PAYMENT_URL_KEY = "url";

    public static final class ProvidedSerialCodec extends SerialCodecBase<InitiateDelegatedPaymentResult> {

        public ProvidedSerialCodec() {
            super(InitiateDelegatedPaymentResult.class, CODEC_ID);
        }

        @Override
        public void encodeToJson(InitiateDelegatedPaymentResult arg, WritableJsonObject json) {
            json.set(DELEGATED_PAYMENT_URL_KEY, arg.delegatedPaymentUrl);
        }

        @Override
        public InitiateDelegatedPaymentResult decodeFromJson(JsonObject json) {
            return new InitiateDelegatedPaymentResult(
                    json.getString(DELEGATED_PAYMENT_URL_KEY)
            );
        }
    }

}
