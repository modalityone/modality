package one.modality.ecommerce.payment.delegated.buscall.serial;

import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;

import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentArgument;

public final class InitiateDelegatedPaymentArgumentSerialCodec
        extends SerialCodecBase<InitiateDelegatedPaymentArgument> {

    private static final String CODEC_ID = "InitiateDelegatedPaymentArgument";
    private static final String DESCRIPTION_KEY = "description";
    private static final String AMOUNT_KEY = "amount";
    private static final String CURRENCY_KEY = "currency";

    public InitiateDelegatedPaymentArgumentSerialCodec() {
        super(InitiateDelegatedPaymentArgument.class, CODEC_ID);
    }

    @Override
    public void encodeToJson(InitiateDelegatedPaymentArgument arg, JsonObject json) {
        json.set(DESCRIPTION_KEY, arg.getDescription());
        json.set(AMOUNT_KEY, arg.getAmount());
        json.set(CURRENCY_KEY, arg.getCurrency());
    }

    @Override
    public InitiateDelegatedPaymentArgument decodeFromJson(ReadOnlyJsonObject json) {
        return new InitiateDelegatedPaymentArgument(
                json.getString(DESCRIPTION_KEY),
                json.get(AMOUNT_KEY),
                json.getString(CURRENCY_KEY));
    }
}
