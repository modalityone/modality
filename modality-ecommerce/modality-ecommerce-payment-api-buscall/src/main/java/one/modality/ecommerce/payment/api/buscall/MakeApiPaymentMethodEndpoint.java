package one.modality.ecommerce.payment.api.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.payment.api.ApiPaymentService;
import one.modality.ecommerce.payment.api.MakeApiPaymentArgument;
import one.modality.ecommerce.payment.api.MakeApiPaymentResult;

/**
 * @author Bruno Salmon
 */
public final class MakeApiPaymentMethodEndpoint extends AsyncFunctionBusCallEndpoint<MakeApiPaymentArgument, MakeApiPaymentResult> {

    public MakeApiPaymentMethodEndpoint() {
        super(ApiPaymentServiceBusAddress.MAKE_API_PAYMENT_METHOD_ADDRESS, ApiPaymentService::makeApiPayment);
    }

}
