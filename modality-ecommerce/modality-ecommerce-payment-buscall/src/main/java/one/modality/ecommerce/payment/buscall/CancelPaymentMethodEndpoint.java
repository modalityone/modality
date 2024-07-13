package one.modality.ecommerce.payment.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.payment.CancelPaymentArgument;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.PaymentService;

/**
 * @author Bruno Salmon
 */
public final class CancelPaymentMethodEndpoint extends AsyncFunctionBusCallEndpoint<CancelPaymentArgument, CancelPaymentResult> {

    public CancelPaymentMethodEndpoint() {
        super(PaymentServiceBusAddress.CANCEL_PAYMENT_METHOD_ADDRESS, PaymentService::cancelPayment);
    }

}
