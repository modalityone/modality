package one.modality.ecommerce.payment.redirect;

import one.modality.ecommerce.payment.redirect.spi.RedirectPaymentProvider;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.SingleServiceProvider;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public class RedirectPaymentService {

    public static RedirectPaymentProvider getProvider() {
        return SingleServiceProvider.getProvider(RedirectPaymentProvider.class, () -> ServiceLoader.load(RedirectPaymentProvider.class));
    }


    public static Future<InitiateRedirectPaymentResult> initiateRedirectPayment(InitiateRedirectPaymentArgument argument) {
        return getProvider().initiateRedirectPayment(argument);
    }

}
