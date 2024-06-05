package one.modality.ecommerce.payment.spi.impl.remote;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.com.bus.call.BusCallService;
import one.modality.ecommerce.payment.InitiatePaymentArgument;
import one.modality.ecommerce.payment.InitiatePaymentResult;
import one.modality.ecommerce.payment.MakeApiPaymentArgument;
import one.modality.ecommerce.payment.MakeApiPaymentResult;
import one.modality.ecommerce.payment.buscall.PaymentServiceBusAddress;
import one.modality.ecommerce.payment.spi.PaymentServiceProvider;

/**
 * @author Bruno Salmon
 */
public class RemotePaymentServiceProvider implements PaymentServiceProvider {

    @Override
    public Future<InitiatePaymentResult> initiatePayment(InitiatePaymentArgument argument) {
        return BusCallService.call(PaymentServiceBusAddress.INITIATE_PAYMENT_METHOD_ADDRESS, argument);
    }

    @Override
    public Future<MakeApiPaymentResult> makeApiPayment(MakeApiPaymentArgument argument) {
        return BusCallService.call(PaymentServiceBusAddress.MAKE_API_PAYMENT_METHOD_ADDRESS, argument);
    }
}
