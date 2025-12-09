package one.modality.ecommerce.payment.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.InitiatePaymentArgument;
import one.modality.ecommerce.payment.PaymentFormType;

/**
 * @author Bruno Salmon
 */
public final class InitiatePaymentArgumentSerialCodec extends SerialCodecBase<InitiatePaymentArgument> {

    private static final String CODEC_ID = "InitiatePaymentArgument";
    private static final String AMOUNT_KEY = "amount";
    private static final String DOCUMENT_PRIMARY_KEY_KEY = "document";
    private static final String PREFERRED_FORM_TYPE_KEY = "preferredFormType";
    private static final String FAVOR_SEAMLESS_KEY = "seamless";
    private static final String IS_ORIGIN_ON_HTTPS_KEY = "https";
    private static final String RETURN_URL_KEY = "returnUrl";
    private static final String CANCEL_URL_KEY = "cancelUrl";

    public InitiatePaymentArgumentSerialCodec() {
        super(InitiatePaymentArgument.class, CODEC_ID);
    }

    @Override
    public void encode(InitiatePaymentArgument arg, AstObject serial) {
        encodeInteger(serial, AMOUNT_KEY,               arg.amount());
        encodeObject( serial, DOCUMENT_PRIMARY_KEY_KEY, arg.documentPrimaryKey());
        encodeString( serial, PREFERRED_FORM_TYPE_KEY,  arg.preferredFormType().name());
        encodeBoolean(serial, FAVOR_SEAMLESS_KEY,       arg.favorSeamless());
        encodeBoolean(serial, IS_ORIGIN_ON_HTTPS_KEY,   arg.isOriginOnHttps());
        encodeString( serial, RETURN_URL_KEY,           arg.returnUrl());
        encodeString( serial, CANCEL_URL_KEY,           arg.cancelUrl());
    }

    @Override
    public InitiatePaymentArgument decode(ReadOnlyAstObject serial) {
        return new InitiatePaymentArgument(
            decodeInteger(serial, AMOUNT_KEY),
            decodeObject( serial, DOCUMENT_PRIMARY_KEY_KEY),
            PaymentFormType.valueOf(decodeString(serial, PREFERRED_FORM_TYPE_KEY)),
            decodeBoolean(serial, FAVOR_SEAMLESS_KEY),
            decodeBoolean(serial, IS_ORIGIN_ON_HTTPS_KEY),
            decodeString( serial, RETURN_URL_KEY),
            decodeString( serial, CANCEL_URL_KEY)
        );
    }
}
