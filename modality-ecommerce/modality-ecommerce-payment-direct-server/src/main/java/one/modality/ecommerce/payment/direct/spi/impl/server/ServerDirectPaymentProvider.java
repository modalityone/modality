package one.modality.ecommerce.payment.direct.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.serviceloader.MultipleServiceProviders;
import one.modality.ecommerce.payment.direct.GetDirectPaymentGatewayInfosArgument;
import one.modality.ecommerce.payment.direct.GetDirectPaymentGatewayInfosResult;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentArgument;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentResult;
import one.modality.ecommerce.payment.direct.spi.DirectPaymentProvider;
import one.modality.ecommerce.payment.gateway.direct.spi.DirectPaymentGatewayProvider;

import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public class ServerDirectPaymentProvider implements DirectPaymentProvider {

    private static List<DirectPaymentGatewayProvider> getProviders() {
        return MultipleServiceProviders.getProviders(DirectPaymentGatewayProvider.class, () -> ServiceLoader.load(DirectPaymentGatewayProvider.class));
    }

    @Override
    public Future<MakeDirectPaymentResult> makeDirectPayment(MakeDirectPaymentArgument argument) {
        List<DirectPaymentGatewayProvider> providers = getProviders();
        if (providers.isEmpty())
            return Future.failedFuture("No direct payment gateway found!");
        return providers.get(0) // Temporary
                .makeDirectPayment(argument);
    }

    @Override
    public Future<GetDirectPaymentGatewayInfosResult> getDirectPaymentGatewayInfos(GetDirectPaymentGatewayInfosArgument argument) {
        return Future.succeededFuture(getDirectPaymentGatewayInfosInstant(argument));
    }

    public GetDirectPaymentGatewayInfosResult getDirectPaymentGatewayInfosInstant(GetDirectPaymentGatewayInfosArgument argument) {
        getProviders();
        return null;
    }
}
