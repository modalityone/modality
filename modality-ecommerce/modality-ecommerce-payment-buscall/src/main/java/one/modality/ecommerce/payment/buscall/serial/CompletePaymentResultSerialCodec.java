package one.modality.ecommerce.payment.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.CompletePaymentResult;
import one.modality.ecommerce.payment.PaymentFailureReason;
import one.modality.ecommerce.payment.PaymentStatus;

/**
 * @author Bruno Salmon
 */
public final class CompletePaymentResultSerialCodec extends SerialCodecBase<CompletePaymentResult> {

    private static final String CODEC_ID = "CompletePaymentResult";

    private static final String STATUS_KEY = "status";
    private static final String FAILURE_REASON_KEY = "failureReason";

    public CompletePaymentResultSerialCodec() {
        super(CompletePaymentResult.class, CODEC_ID);
    }

    @Override
    public void encode(CompletePaymentResult arg, AstObject serial) {
        encodeString(serial, STATUS_KEY, arg.paymentStatus().name());
        PaymentFailureReason failureReason = arg.failureReason();
        if (failureReason != null) {
            encodeString(serial, FAILURE_REASON_KEY, failureReason.name());
        }
    }

    @Override
    public CompletePaymentResult decode(ReadOnlyAstObject serial) {
        String failureReasonName = decodeString(serial, FAILURE_REASON_KEY);
        return new CompletePaymentResult(
                PaymentStatus.valueOf(decodeString(serial, STATUS_KEY)),
                failureReasonName == null ? null : PaymentFailureReason.valueOf(failureReasonName)
        );
    }
}
