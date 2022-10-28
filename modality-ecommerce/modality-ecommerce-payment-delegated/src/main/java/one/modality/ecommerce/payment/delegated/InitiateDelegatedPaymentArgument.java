package one.modality.ecommerce.payment.delegated;

import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.WritableJsonObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;

/**
 * @author Bruno Salmon
 */
public class InitiateDelegatedPaymentArgument {

    private final String description;
    private final int amount;
    private final String currency;

    public InitiateDelegatedPaymentArgument(String description, int amount, String currency) {
        this.description = description;
        this.amount = amount;
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    /**************************************
     *           Serial Codec             *
     * ***********************************/

    private static final String CODEC_ID = "InitiateDelegatedPaymentArgument";
    private static final String DESCRIPTION_KEY = "description";
    private static final String AMOUNT_KEY = "amount";
    private static final String CURRENCY_KEY = "currency";

    public static final class ProvidedSerialCodec extends SerialCodecBase<InitiateDelegatedPaymentArgument> {

        public ProvidedSerialCodec() {
            super(InitiateDelegatedPaymentArgument.class, CODEC_ID);
        }

        @Override
        public void encodeToJson(InitiateDelegatedPaymentArgument arg, WritableJsonObject json) {
            json.set(DESCRIPTION_KEY, arg.getDescription());
            json.set(AMOUNT_KEY, arg.getAmount());
            json.set(CURRENCY_KEY, arg.getCurrency());
        }

        @Override
        public InitiateDelegatedPaymentArgument decodeFromJson(JsonObject json) {
            return new InitiateDelegatedPaymentArgument(
                    json.getString(DESCRIPTION_KEY),
                    json.get(AMOUNT_KEY),
                    json.getString(CURRENCY_KEY)
            );
        }
    }

}
