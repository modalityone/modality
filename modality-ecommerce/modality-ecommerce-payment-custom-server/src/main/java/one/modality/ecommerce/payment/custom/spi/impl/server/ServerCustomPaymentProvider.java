package one.modality.ecommerce.payment.custom.spi.impl.server;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentArgument;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentResult;
import one.modality.ecommerce.payment.custom.spi.CustomPaymentProvider;
import one.modality.ecommerce.payment.gateway.custom.spi.CustomPaymentGatewayProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public class ServerCustomPaymentProvider implements CustomPaymentProvider {

    @Override
    public Future<InitiateCustomPaymentResult> initiateCustomPayment(InitiateCustomPaymentArgument argument) {
        Iterator<CustomPaymentGatewayProvider> it = getDelegatedPaymentGatewayProviders().iterator();
        if (it.hasNext())
            return it.next().initiateCustomPayment(argument);
        return Future.failedFuture(new IllegalStateException("No delegated payment gateway found!"));
    }

    protected ServiceLoader<CustomPaymentGatewayProvider> getDelegatedPaymentGatewayProviders() {
        return ServiceLoader.load(CustomPaymentGatewayProvider.class);
    }

}
