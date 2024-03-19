package one.modality.ecommerce.payment.direct.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentResult;

/**************************************
 *           Serial Codec             *
 * ***********************************/

public final class MakeDirectPaymentResultSerialCodec extends SerialCodecBase<MakeDirectPaymentResult> {

    private static final String CODEC_ID = "DirectPaymentResult";
    private static final String SUCCESS_KEY = "success";

    public MakeDirectPaymentResultSerialCodec() {
        super(MakeDirectPaymentResult.class, CODEC_ID);
    }

    @Override
    public void encodeToJson(MakeDirectPaymentResult arg, AstObject json) {
        json.set(SUCCESS_KEY, arg.isSuccess());
    }

    @Override
    public MakeDirectPaymentResult decodeFromJson(ReadOnlyAstObject json) {
        return new MakeDirectPaymentResult(
                json.getBoolean(SUCCESS_KEY)
        );
    }
}
