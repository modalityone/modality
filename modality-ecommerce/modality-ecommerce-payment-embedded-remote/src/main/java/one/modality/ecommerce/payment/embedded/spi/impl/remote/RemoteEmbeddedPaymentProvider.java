package one.modality.ecommerce.payment.embedded.spi.impl.remote;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.com.bus.call.BusCallService;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentArgument;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentResult;
import one.modality.ecommerce.payment.embedded.buscall.EmbeddedPaymentServiceBusAddress;
import one.modality.ecommerce.payment.embedded.spi.EmbeddedPaymentProvider;

/**
 * @author Bruno Salmon
 */
public class RemoteEmbeddedPaymentProvider implements EmbeddedPaymentProvider {

    @Override
    public Future<InitiateEmbeddedPaymentResult> initiateEmbeddedPayment(InitiateEmbeddedPaymentArgument argument) {
        return BusCallService.call(EmbeddedPaymentServiceBusAddress.INITIATE_EMBEDDED_PAYMENT_METHOD_ADDRESS, argument);
    }

}
