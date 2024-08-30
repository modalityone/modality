package one.modality.ecommerce.payment.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.CancelPaymentResult;

/**
 * @author Bruno Salmon
 */
public final class CancelPaymentResultSerialCodec extends SerialCodecBase<CancelPaymentResult> {

    private static final String CODEC_ID = "CancelPaymentResult";

    public CancelPaymentResultSerialCodec() {
        super(CancelPaymentResult.class, CODEC_ID);
    }

    @Override
    public void encode(CancelPaymentResult arg, AstObject serial) {
    }

    @Override
    public CancelPaymentResult decode(ReadOnlyAstObject serial) {
        return new CancelPaymentResult(
        );
    }
}
