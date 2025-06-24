package one.modality.ecommerce.payment.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.InitiatePaymentResult;
import one.modality.ecommerce.payment.SandboxCard;

/**
 * @author Bruno Salmon
 */
public final class InitiatePaymentResultSerialCodec extends SerialCodecBase<InitiatePaymentResult> {

    private static final String CODEC_ID = "InitiatePaymentResult";
    private static final String PAYMENT_PRIMARY_KEY_KEY = "payment";
    private static final String LIVE_KEY = "live";
    private static final String SEAMLESS_KEY = "seamless";
    private static final String HTML_CONTENT_KEY = "htmlContent";
    private static final String URL_KEY = "url";
    private static final String REDIRECT_KEY = "redirect";
    private static final String HAS_HTML_PAY_BUTTON_KEY = "hasHtmlPayButton";
    private static final String GATEWAY_NAME_KEY = "gateway";
    private static final String SANDBOX_CARDS_KEY = "sandboxCards";

    public InitiatePaymentResultSerialCodec() {
        super(InitiatePaymentResult.class, CODEC_ID);
    }

    @Override
    public void encode(InitiatePaymentResult arg, AstObject serial) {
        encodeObject( serial, PAYMENT_PRIMARY_KEY_KEY, arg.getPaymentPrimaryKey());
        encodeBoolean(serial, LIVE_KEY,                arg.isLive());
        encodeBoolean(serial, SEAMLESS_KEY,            arg.isSeamless());
        encodeString( serial, HTML_CONTENT_KEY,        arg.getHtmlContent());
        encodeString( serial, URL_KEY,                 arg.getUrl());
        encodeBoolean(serial, REDIRECT_KEY,            arg.isRedirect());
        encodeBoolean(serial, HAS_HTML_PAY_BUTTON_KEY, arg.hasHtmlPayButton());
        encodeString( serial, GATEWAY_NAME_KEY,        arg.getGatewayName());
        encodeArray(  serial, SANDBOX_CARDS_KEY,       arg.getSandboxCards());
    }

    @Override
    public InitiatePaymentResult decode(ReadOnlyAstObject serial) {
        return new InitiatePaymentResult(
                decodeObject( serial, PAYMENT_PRIMARY_KEY_KEY),
                decodeBoolean(serial, LIVE_KEY),
                decodeBoolean(serial, SEAMLESS_KEY),
                decodeString( serial, HTML_CONTENT_KEY),
                decodeString( serial, URL_KEY),
                decodeBoolean(serial, REDIRECT_KEY),
                decodeBoolean(serial, HAS_HTML_PAY_BUTTON_KEY),
                decodeString( serial, GATEWAY_NAME_KEY),
                decodeArray(  serial, SANDBOX_CARDS_KEY, SandboxCard.class)
        );
    }
}
