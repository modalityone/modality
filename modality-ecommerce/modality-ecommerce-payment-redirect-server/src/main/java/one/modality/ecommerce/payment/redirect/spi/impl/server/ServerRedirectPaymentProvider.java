package one.modality.ecommerce.payment.redirect.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.MultipleServiceProviders;
import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentArgument;
import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentResult;
import one.modality.ecommerce.payment.redirect.spi.RedirectPaymentProvider;
import one.modality.ecommerce.payment.gateway.redirect.spi.RedirectPaymentGatewayProvider;
import one.modality.ecommerce.payment.gateway.redirect.spi.InitiateRedirectPaymentGatewayArgument;

import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public class ServerRedirectPaymentProvider implements RedirectPaymentProvider {

    // @TODO All information should be extracted from the database
    protected static String STRIPE_API_KEY = "sk_test_51LZvA1AHcMXMUjXlSTy6TnE1bSpUkgY46eniZju5bGZ9uFXCYsVKZfVTXqET2wRTU7ZoIyLqaP7icmPDIosQzn0L005tfSqEeT";
    protected static String PAYMENT_SUCCESS_URL = "https://payments.modality.one/success/";
    protected static String PAYMENT_CANCEL_URL = "https://payments.modality.one/cancel/";

    private static List<RedirectPaymentGatewayProvider> getProviders() {
        return MultipleServiceProviders.getProviders(RedirectPaymentGatewayProvider.class, () -> ServiceLoader.load(RedirectPaymentGatewayProvider.class));
    }

    @Override
    public Future<InitiateRedirectPaymentResult> initiateRedirectPayment(InitiateRedirectPaymentArgument argument) {
        List<RedirectPaymentGatewayProvider> providers = getProviders();
        if (providers.isEmpty())
            return Future.failedFuture("No redirect payment gateway found!");
        return providers.get(0) // Temporary
                .initiateRedirectPayment(new InitiateRedirectPaymentGatewayArgument(argument, STRIPE_API_KEY, PAYMENT_SUCCESS_URL, PAYMENT_CANCEL_URL));
    }

}
