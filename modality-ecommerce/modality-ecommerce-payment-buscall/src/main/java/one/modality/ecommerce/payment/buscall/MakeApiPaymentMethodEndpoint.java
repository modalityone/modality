package one.modality.ecommerce.payment.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.payment.MakeApiPaymentArgument;
import one.modality.ecommerce.payment.MakeApiPaymentResult;
import one.modality.ecommerce.payment.PaymentService;

/**
 * @author Bruno Salmon
 */
public final class MakeApiPaymentMethodEndpoint extends AsyncFunctionBusCallEndpoint<MakeApiPaymentArgument, MakeApiPaymentResult> {

    public MakeApiPaymentMethodEndpoint() {
        super(PaymentServiceBusAddress.MAKE_API_PAYMENT_METHOD_ADDRESS, PaymentService::makeApiPayment);
    }

}
