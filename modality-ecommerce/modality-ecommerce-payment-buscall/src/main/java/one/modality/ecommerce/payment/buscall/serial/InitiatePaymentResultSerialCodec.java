package one.modality.ecommerce.payment.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.InitiatePaymentResult;
import one.modality.ecommerce.payment.PaymentFormType;
import one.modality.ecommerce.payment.SandboxCard;

/**
 * @author Bruno Salmon
 */
public final class InitiatePaymentResultSerialCodec extends SerialCodecBase<InitiatePaymentResult> {

    private static final String CODEC_ID = "InitiatePaymentResult";
    private static final String PAYMENT_PRIMARY_KEY_KEY = "payment";
    private static final String AMOUNT_KEY = "amount";
    private static final String LIVE_KEY = "live";
    private static final String SEAMLESS_KEY = "seamless";
    private static final String HTML_CONTENT_KEY = "htmlContent";
    private static final String URL_KEY = "url";
    private static final String PAYMENT_FORM_TYPE_KEY = "formType";
    private static final String HAS_HTML_PAY_BUTTON_KEY = "hasHtmlPayButton";
    private static final String GATEWAY_NAME_KEY = "gateway";
    private static final String SANDBOX_CARDS_KEY = "sandboxCards";

    public InitiatePaymentResultSerialCodec() {
        super(InitiatePaymentResult.class, CODEC_ID);
    }

    @Override
    public void encode(InitiatePaymentResult arg, AstObject serial) {
        encodeString( serial, GATEWAY_NAME_KEY,        arg.gatewayName());
        encodeObject( serial, PAYMENT_PRIMARY_KEY_KEY, arg.paymentPrimaryKey());
        encodeInteger(serial, AMOUNT_KEY,              arg.amount());
        encodeBoolean(serial, LIVE_KEY,                arg.isLive());
        encodeString( serial, URL_KEY,                 arg.url());
        encodeString( serial, PAYMENT_FORM_TYPE_KEY,   arg.formType().name());
        encodeString( serial, HTML_CONTENT_KEY,        arg.htmlContent());
        encodeBoolean(serial, SEAMLESS_KEY,            arg.isSeamless());
        encodeBoolean(serial, HAS_HTML_PAY_BUTTON_KEY, arg.hasHtmlPayButton());
        encodeArray(  serial, SANDBOX_CARDS_KEY,       arg.sandboxCards());
    }

    @Override
    public InitiatePaymentResult decode(ReadOnlyAstObject serial) {
        return new InitiatePaymentResult(
            decodeString( serial, GATEWAY_NAME_KEY),
            decodeObject( serial, PAYMENT_PRIMARY_KEY_KEY),
            decodeInteger(serial, AMOUNT_KEY),
            decodeBoolean(serial, LIVE_KEY),
            decodeString( serial, URL_KEY),
            PaymentFormType.valueOf(decodeString(serial, PAYMENT_FORM_TYPE_KEY)),
            decodeString( serial, HTML_CONTENT_KEY),
            decodeBoolean(serial, SEAMLESS_KEY),
            decodeBoolean(serial, HAS_HTML_PAY_BUTTON_KEY),
            decodeArray(  serial, SANDBOX_CARDS_KEY, SandboxCard.class)
        );
    }
}
