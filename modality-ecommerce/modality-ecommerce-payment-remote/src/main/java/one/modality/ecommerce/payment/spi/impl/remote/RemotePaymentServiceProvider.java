package one.modality.ecommerce.payment.spi.impl.remote;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.com.bus.call.BusCallService;
import one.modality.ecommerce.payment.*;
import one.modality.ecommerce.payment.buscall.PaymentServiceBusAddress;
import one.modality.ecommerce.payment.spi.PaymentServiceProvider;

import java.util.Map;

/**
 * @author Bruno Salmon
 */
public class RemotePaymentServiceProvider implements PaymentServiceProvider {

    @Override
    public Future<InitiatePaymentResult> initiatePayment(InitiatePaymentArgument argument) {
        return BusCallService.call(PaymentServiceBusAddress.INITIATE_PAYMENT_METHOD_ADDRESS, argument);
    }

    @Override
    public Future<CompletePaymentResult> completePayment(CompletePaymentArgument argument) {
        return BusCallService.call(PaymentServiceBusAddress.COMPLETE_PAYMENT_METHOD_ADDRESS, argument);
    }

    @Override
    public Future<CancelPaymentResult> cancelPayment(CancelPaymentArgument argument) {
        return BusCallService.call(PaymentServiceBusAddress.CANCEL_PAYMENT_METHOD_ADDRESS, argument);
    }

    // Internal server-side method only (no serialisation support)

    @Override
    public Future<Map<String, String>> loadPaymentGatewayParameters(Object paymentId, boolean live) {
        return Future.failedFuture("loadPaymentGatewayParameters() is an internal server-side method only");
    }

    @Override
    public Future<Void> updatePaymentStatus(UpdatePaymentStatusArgument argument) {
        return Future.failedFuture("completePayment() is an internal server-side method only");
    }
}
