package one.modality.ecommerce.payment.api.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.api.MakeApiPaymentResult;

/**************************************
 *           Serial Codec             *
 * ***********************************/

public final class MakeApiPaymentResultSerialCodec extends SerialCodecBase<MakeApiPaymentResult> {

    private static final String CODEC_ID = "MakeApiPaymentResult";
    private static final String SUCCESS_KEY = "success";

    public MakeApiPaymentResultSerialCodec() {
        super(MakeApiPaymentResult.class, CODEC_ID);
    }

    @Override
    public void encode(MakeApiPaymentResult arg, AstObject serial) {
        encodeBoolean(serial, SUCCESS_KEY, arg.isSuccess());
    }

    @Override
    public MakeApiPaymentResult decode(ReadOnlyAstObject serial) {
        return new MakeApiPaymentResult(
                decodeBoolean(serial, SUCCESS_KEY)
        );
    }
}
