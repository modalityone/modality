package one.modality.ecommerce.payment.delegated.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.serviceloader.MultipleServiceProviders;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentArgument;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentResult;
import one.modality.ecommerce.payment.delegated.spi.DelegatedPaymentProvider;
import one.modality.ecommerce.payment.gateway.delegated.spi.DelegatedPaymentGatewayProvider;
import one.modality.ecommerce.payment.gateway.delegated.spi.InitiateDelegatedPaymentGatewayArgument;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public class ServerDelegatedPaymentProvider implements DelegatedPaymentProvider {

    // @TODO All information should be extracted from the database
    protected static String STRIPE_API_KEY = "sk_test_51LZvA1AHcMXMUjXlSTy6TnE1bSpUkgY46eniZju5bGZ9uFXCYsVKZfVTXqET2wRTU7ZoIyLqaP7icmPDIosQzn0L005tfSqEeT";
    protected static String PAYMENT_SUCCESS_URL = "https://payments.modality.one/success/";
    protected static String PAYMENT_CANCEL_URL = "https://payments.modality.one/cancel/";

    private static List<DelegatedPaymentGatewayProvider> getProviders() {
        return MultipleServiceProviders.getProviders(DelegatedPaymentGatewayProvider.class, () -> ServiceLoader.load(DelegatedPaymentGatewayProvider.class));
    }

    @Override
    public Future<InitiateDelegatedPaymentResult> initiateDelegatedPayment(InitiateDelegatedPaymentArgument argument) {
        List<DelegatedPaymentGatewayProvider> providers = getProviders();
        if (providers.isEmpty())
            return Future.failedFuture("No delegated payment gateway found!");
        return providers.get(0) // Temporary
                .initiateDelegatedPayment(new InitiateDelegatedPaymentGatewayArgument(argument, STRIPE_API_KEY, PAYMENT_SUCCESS_URL, PAYMENT_CANCEL_URL));
    }

}
