package one.modality.ecommerce.payment.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.payment.*;

/**
 * @author Bruno Salmon
 */
public final class CompletePaymentMethodEndpoint extends AsyncFunctionBusCallEndpoint<CompletePaymentArgument, CompletePaymentResult> {

    public CompletePaymentMethodEndpoint() {
        super(PaymentServiceBusAddress.COMPLETE_PAYMENT_METHOD_ADDRESS, PaymentService::completePayment);
    }

}
