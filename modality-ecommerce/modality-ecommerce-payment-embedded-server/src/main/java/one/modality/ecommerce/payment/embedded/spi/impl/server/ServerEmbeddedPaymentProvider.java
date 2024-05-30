package one.modality.ecommerce.payment.embedded.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.MultipleServiceProviders;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentArgument;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentResult;
import one.modality.ecommerce.payment.embedded.spi.EmbeddedPaymentProvider;
import one.modality.ecommerce.payment.embedded.gateway.spi.EmbeddedPaymentGatewayProvider;

import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public class ServerEmbeddedPaymentProvider implements EmbeddedPaymentProvider {

    private static List<EmbeddedPaymentGatewayProvider> getProviders() {
        return MultipleServiceProviders.getProviders(EmbeddedPaymentGatewayProvider.class, () -> ServiceLoader.load(EmbeddedPaymentGatewayProvider.class));
    }

    @Override
    public Future<InitiateEmbeddedPaymentResult> initiateEmbeddedPayment(InitiateEmbeddedPaymentArgument argument) {
        List<EmbeddedPaymentGatewayProvider> providers = getProviders();
        if (providers.isEmpty())
            return Future.failedFuture(new IllegalStateException("No embedded payment gateway found!"));
        return providers.get(0) // Temporary
                .initiateEmbeddedPayment(argument);
    }

}
