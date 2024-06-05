package one.modality.ecommerce.payment;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.SingleServiceProvider;
import one.modality.ecommerce.payment.spi.PaymentServiceProvider;

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

    public static Future<MakeApiPaymentResult> makeApiPayment(MakeApiPaymentArgument argument) {
        return getProvider().makeApiPayment(argument);
    }

}
