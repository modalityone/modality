package one.modality.ecommerce.payment.custom.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.payment.custom.CustomPaymentService;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentArgument;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentResult;

/**
 * @author Bruno Salmon
 */
public final class InitiateCustomPaymentMethodEndpoint extends AsyncFunctionBusCallEndpoint<InitiateCustomPaymentArgument, InitiateCustomPaymentResult> {

    public InitiateCustomPaymentMethodEndpoint() {
        super(CustomPaymentServiceBusAddress.INITIATE_CUSTOM_PAYMENT_METHOD_ADDRESS, CustomPaymentService::initiateCustomPayment);
    }

}
