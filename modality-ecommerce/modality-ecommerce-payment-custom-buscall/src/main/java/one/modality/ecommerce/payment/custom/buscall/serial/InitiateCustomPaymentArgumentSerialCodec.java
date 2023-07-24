package one.modality.ecommerce.payment.custom.buscall.serial;

import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;

import one.modality.ecommerce.payment.custom.InitiateCustomPaymentArgument;

public final class InitiateCustomPaymentArgumentSerialCodec
        extends SerialCodecBase<InitiateCustomPaymentArgument> {

    private static final String CODEC_ID = "InitiateCustomPaymentArgument";
    private static final String AMOUNT_KEY = "amount";
    private static final String CURRENCY_KEY = "currency";
    private static final String PRODUCT_NAME_KEY = "productName";
    private static final String QUANTITY_KEY = "quantity";
    private static final String CUSTOMER_ID_KEY = "customerId";
    private static final String SUCCESS_URL_KEY = "successUrl";
    private static final String FAIL_URL_KEY = "failUrl";

    public InitiateCustomPaymentArgumentSerialCodec() {
        super(InitiateCustomPaymentArgument.class, CODEC_ID);
    }

    @Override
    public void encodeToJson(InitiateCustomPaymentArgument arg, JsonObject json) {
        json.set(AMOUNT_KEY, arg.getAmount());
        json.set(CURRENCY_KEY, arg.getCurrency());
        json.set(PRODUCT_NAME_KEY, arg.getProductName());
        json.set(QUANTITY_KEY, arg.getQuantity());
        json.set(CUSTOMER_ID_KEY, arg.getCustomerId());
        json.set(SUCCESS_URL_KEY, arg.getSuccessUrl());
        json.set(FAIL_URL_KEY, arg.getFailUrl());
    }

    @Override
    public InitiateCustomPaymentArgument decodeFromJson(ReadOnlyJsonObject json) {
        return new InitiateCustomPaymentArgument(
                json.getLong(AMOUNT_KEY),
                json.getString(CURRENCY_KEY),
                json.getString(PRODUCT_NAME_KEY),
                json.getLong(QUANTITY_KEY),
                json.getString(CUSTOMER_ID_KEY),
                json.getString(SUCCESS_URL_KEY),
                json.getString(FAIL_URL_KEY));
    }
}
