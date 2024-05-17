package one.modality.ecommerce.payment.custom.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentArgument;

public final class InitiateCustomPaymentArgumentSerialCodec extends SerialCodecBase<InitiateCustomPaymentArgument> {

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
    public void encode(InitiateCustomPaymentArgument arg, AstObject serial) {
        encodeLong(  serial, AMOUNT_KEY,       arg.getAmount());
        encodeString(serial, CURRENCY_KEY,     arg.getCurrency());
        encodeString(serial, PRODUCT_NAME_KEY, arg.getProductName());
        encodeLong(  serial, QUANTITY_KEY,     arg.getQuantity());
        encodeString(serial, CUSTOMER_ID_KEY,  arg.getCustomerId());
        encodeString(serial, SUCCESS_URL_KEY,  arg.getSuccessUrl());
        encodeString(serial, FAIL_URL_KEY,     arg.getFailUrl());
    }

    @Override
    public InitiateCustomPaymentArgument decode(ReadOnlyAstObject serial) {
        return new InitiateCustomPaymentArgument(
                decodeLong(  serial, AMOUNT_KEY),
                decodeString(serial, CURRENCY_KEY),
                decodeString(serial, PRODUCT_NAME_KEY),
                decodeLong(  serial, QUANTITY_KEY),
                decodeString(serial, CUSTOMER_ID_KEY),
                decodeString(serial, SUCCESS_URL_KEY),
                decodeString(serial, FAIL_URL_KEY)
        );
    }
}
