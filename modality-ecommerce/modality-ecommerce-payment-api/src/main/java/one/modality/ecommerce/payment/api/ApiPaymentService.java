package one.modality.ecommerce.payment.api;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.service.SingleServiceProvider;
import one.modality.ecommerce.payment.api.spi.ApiPaymentProvider;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public final class ApiPaymentService {

    public static ApiPaymentProvider getProvider() {
        return SingleServiceProvider.getProvider(ApiPaymentProvider.class, () -> ServiceLoader.load(ApiPaymentProvider.class));
    }

    public static Future<MakeApiPaymentResult> makeApiPayment(MakeApiPaymentArgument argument) {
        return getProvider().makeApiPayment(argument);
    }

    public static Future<GetApiPaymentGatewayInfosResult> getApiPaymentGatewayInfos(GetApiPaymentGatewayInfosArgument argument) {
        return getProvider().getApiPaymentGatewayInfos(argument);
    }

}
