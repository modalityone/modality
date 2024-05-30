package one.modality.ecommerce.payment.embedded;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.SingleServiceProvider;
import one.modality.ecommerce.payment.embedded.spi.EmbeddedPaymentProvider;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public final class EmbeddedPaymentService {

    public static EmbeddedPaymentProvider getProvider() {
        return SingleServiceProvider.getProvider(EmbeddedPaymentProvider.class, () -> ServiceLoader.load(EmbeddedPaymentProvider.class));
    }


    public static Future<InitiateEmbeddedPaymentResult> initiateEmbeddedPayment(InitiateEmbeddedPaymentArgument argument) {
        return getProvider().initiateEmbeddedPayment(argument);
    }
}
