package one.modality.ecommerce.payment.api.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.MultipleServiceProviders;
import one.modality.ecommerce.payment.api.GetApiPaymentGatewayInfosArgument;
import one.modality.ecommerce.payment.api.GetApiPaymentGatewayInfosResult;
import one.modality.ecommerce.payment.api.MakeApiPaymentArgument;
import one.modality.ecommerce.payment.api.MakeApiPaymentResult;
import one.modality.ecommerce.payment.api.spi.ApiPaymentProvider;
import one.modality.ecommerce.payment.api.gateway.spi.ApiPaymentGatewayProvider;

import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public class ServerApiPaymentProvider implements ApiPaymentProvider {

    private static List<ApiPaymentGatewayProvider> getProviders() {
        return MultipleServiceProviders.getProviders(ApiPaymentGatewayProvider.class, () -> ServiceLoader.load(ApiPaymentGatewayProvider.class));
    }

    @Override
    public Future<MakeApiPaymentResult> makeApiPayment(MakeApiPaymentArgument argument) {
        List<ApiPaymentGatewayProvider> providers = getProviders();
        if (providers.isEmpty())
            return Future.failedFuture("No api payment gateway found!");
        return providers.get(0) // Temporary
                .makeApiPayment(argument);
    }

    @Override
    public Future<GetApiPaymentGatewayInfosResult> getApiPaymentGatewayInfos(GetApiPaymentGatewayInfosArgument argument) {
        return Future.succeededFuture(getDirectPaymentGatewayInfosInstant(argument));
    }

    public GetApiPaymentGatewayInfosResult getDirectPaymentGatewayInfosInstant(GetApiPaymentGatewayInfosArgument argument) {
        getProviders();
        return null;
    }
}
