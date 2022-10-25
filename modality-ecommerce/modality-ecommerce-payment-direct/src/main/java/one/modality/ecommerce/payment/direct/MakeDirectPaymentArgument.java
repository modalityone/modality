package one.modality.ecommerce.payment.direct;

import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.WritableJsonObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;

/**
 * @author Bruno Salmon
 */
public class MakeDirectPaymentArgument {

    private final int amount;
    private final String currency;
    private final String ccNumber;
    private final String ccExpiry;

    public MakeDirectPaymentArgument(int amount, String currency, String ccNumber, String ccExpiry) {
        this.amount = amount;
        this.currency = currency;
        this.ccNumber = ccNumber;
        this.ccExpiry = ccExpiry;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCcNumber() {
        return ccNumber;
    }

    public String getCcExpiry() {
        return ccExpiry;
    }

    /**************************************
     *           Serial Codec             *
     * ***********************************/

    public static final class ProvidedSerialCodec extends SerialCodecBase<MakeDirectPaymentArgument> {

        private static final String CODEC_ID = "DirectPaymentArgument";
        private static final String AMOUNT_KEY = "amount";
        private static final String CURRENCY_KEY = "currency";
        private static final String CC_NUMBER_KEY = "ccNumber";
        private static final String CC_EXPIRY_KEY = "ccExpiry";

        public ProvidedSerialCodec() {
            super(MakeDirectPaymentArgument.class, CODEC_ID);
        }

        @Override
        public void encodeToJson(MakeDirectPaymentArgument arg, WritableJsonObject json) {
            json.set(AMOUNT_KEY, arg.getAmount());
            json.set(CURRENCY_KEY, arg.getCurrency());
            json.set(CC_NUMBER_KEY, arg.getCcNumber());
            json.set(CC_EXPIRY_KEY, arg.getCcExpiry());
        }

        @Override
        public MakeDirectPaymentArgument decodeFromJson(JsonObject json) {
            return new MakeDirectPaymentArgument(
                    json.get(AMOUNT_KEY),
                    json.getString(CURRENCY_KEY),
                    json.getString(CC_NUMBER_KEY),
                    json.getString(CC_EXPIRY_KEY)
            );
        }
    }

}
