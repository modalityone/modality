package one.modality.ecommerce.payment.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.payment.InitiatePaymentResult;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.InitiatePaymentArgument;

/**
 * @author Bruno Salmon
 */
public final class InitiatePaymentMethodEndpoint extends AsyncFunctionBusCallEndpoint<InitiatePaymentArgument, InitiatePaymentResult> {

    public InitiatePaymentMethodEndpoint() {
        super(PaymentServiceBusAddress.INITIATE_PAYMENT_METHOD_ADDRESS, PaymentService::initiatePayment);
    }

}
