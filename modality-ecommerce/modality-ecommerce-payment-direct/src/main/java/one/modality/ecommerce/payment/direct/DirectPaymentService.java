package one.modality.ecommerce.payment.direct;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.serviceloader.SingleServiceProvider;
import one.modality.ecommerce.payment.direct.spi.DirectPaymentProvider;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public final class DirectPaymentService {

    public static DirectPaymentProvider getProvider() {
        return SingleServiceProvider.getProvider(DirectPaymentProvider.class, () -> ServiceLoader.load(DirectPaymentProvider.class));
    }

    public static Future<MakeDirectPaymentResult> makeDirectPayment(MakeDirectPaymentArgument argument) {
        return getProvider().makeDirectPayment(argument);
    }

    public static Future<GetDirectPaymentGatewayInfosResult> getDirectPaymentGatewayInfos(GetDirectPaymentGatewayInfosArgument argument) {
        return null;
    }

}
