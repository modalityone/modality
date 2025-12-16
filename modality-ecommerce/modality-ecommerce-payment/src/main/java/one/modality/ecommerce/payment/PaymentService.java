package one.modality.ecommerce.payment;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.SingleServiceProvider;
import one.modality.ecommerce.payment.spi.PaymentServiceProvider;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public final class PaymentService {

    private static PaymentServiceProvider getProvider() {
        return SingleServiceProvider.getProvider(PaymentServiceProvider.class, () -> ServiceLoader.load(PaymentServiceProvider.class));
    }

    public static Future<InitiatePaymentResult> initiatePayment(InitiatePaymentArgument argument) {
        return getProvider().initiatePayment(argument);
    }

    public static Future<CompletePaymentResult> completePayment(CompletePaymentArgument argument) {
        return getProvider().completePayment(argument);
    }

    public static Future<CancelPaymentResult> cancelPayment(CancelPaymentArgument argument) {
        return getProvider().cancelPayment(argument);
    }

    // Internal server-side method only (no serialization support)

    public static Future<Map<String, String>> loadPaymentGatewayParameters(Object paymentId, boolean live) {
        return getProvider().loadPaymentGatewayParameters(paymentId, live);
    }

    public static Future<Void> updatePaymentStatus(UpdatePaymentStatusArgument argument) {
        return getProvider().updatePaymentStatus(argument);
    }

}
