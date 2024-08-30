package one.modality.ecommerce.payment.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.CompletePaymentArgument;

/**
 * @author Bruno Salmon
 */
public final class CompletePaymentArgumentSerialCodec extends SerialCodecBase<CompletePaymentArgument> {

    private static final String CODEC_ID = "CompletePaymentArgument";

    private static final String PAYMENT_PRIMARY_KEY_KEY = "payment";
    private static final String LIVE_KEY = "live";
    private static final String GATEWAY_NAME_KEY = "gatewayName";
    private static final String GATEWAY_PAYLOAD_KEY = "payload";

    public CompletePaymentArgumentSerialCodec() {
        super(CompletePaymentArgument.class, CODEC_ID);
    }

    @Override
    public void encode(CompletePaymentArgument arg, AstObject serial) {
        encodeObject( serial, PAYMENT_PRIMARY_KEY_KEY, arg.getPaymentPrimaryKey());
        encodeBoolean(serial, LIVE_KEY,                arg.isLive());
        encodeString( serial, GATEWAY_NAME_KEY,        arg.getGatewayName());
        encodeString( serial, GATEWAY_PAYLOAD_KEY,     arg.getGatewayCompletePaymentPayload());
    }

    @Override
    public CompletePaymentArgument decode(ReadOnlyAstObject serial) {
        return new CompletePaymentArgument(
                decodeObject( serial, PAYMENT_PRIMARY_KEY_KEY),
                decodeBoolean(serial, LIVE_KEY),
                decodeString( serial, GATEWAY_NAME_KEY),
                decodeString( serial, GATEWAY_PAYLOAD_KEY)
        );
    }
}
