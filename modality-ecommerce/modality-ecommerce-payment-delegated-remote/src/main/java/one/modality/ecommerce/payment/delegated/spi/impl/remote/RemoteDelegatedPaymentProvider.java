package one.modality.ecommerce.payment.delegated.spi.impl.remote;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.com.bus.call.BusCallService;

import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentArgument;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentResult;
import one.modality.ecommerce.payment.delegated.buscall.DelegatedPaymentServiceBusAddress;
import one.modality.ecommerce.payment.delegated.spi.DelegatedPaymentProvider;

/**
 * @author Bruno Salmon
 */
public class RemoteDelegatedPaymentProvider implements DelegatedPaymentProvider {
    @Override
    public Future<InitiateDelegatedPaymentResult> initiateDelegatedPayment(
            InitiateDelegatedPaymentArgument argument) {
        return BusCallService.call(
                DelegatedPaymentServiceBusAddress.INITIATE_DELEGATED_PAYMENT_METHOD_ADDRESS,
                argument);
    }
}
