package one.modality.ecommerce.payment.embedded.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.payment.embedded.EmbeddedPaymentService;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentArgument;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentResult;

/**
 * @author Bruno Salmon
 */
public final class InitiateEmbeddedPaymentMethodEndpoint extends AsyncFunctionBusCallEndpoint<InitiateEmbeddedPaymentArgument, InitiateEmbeddedPaymentResult> {

    public InitiateEmbeddedPaymentMethodEndpoint() {
        super(EmbeddedPaymentServiceBusAddress.INITIATE_EMBEDDED_PAYMENT_METHOD_ADDRESS, EmbeddedPaymentService::initiateEmbeddedPayment);
    }

}
