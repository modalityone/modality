package one.modality.ecommerce.payment.direct;

import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.WritableJsonObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;

/**
 * @author Bruno Salmon
 */
public class MakeDirectPaymentResult {

    private final boolean success;

    public MakeDirectPaymentResult(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    /**************************************
     *           Serial Codec             *
     * ***********************************/

    public static final class ProvidedSerialCodec extends SerialCodecBase<MakeDirectPaymentResult> {

        private static final String CODEC_ID = "DirectPaymentResult";
        private static final String SUCCESS_KEY = "success";

        public ProvidedSerialCodec() {
            super(MakeDirectPaymentResult.class, CODEC_ID);
        }

        @Override
        public void encodeToJson(MakeDirectPaymentResult arg, WritableJsonObject json) {
            json.set(SUCCESS_KEY, arg.isSuccess());
        }

        @Override
        public MakeDirectPaymentResult decodeFromJson(JsonObject json) {
            return new MakeDirectPaymentResult(
                    json.getBoolean(SUCCESS_KEY)
            );
        }
    }

}
