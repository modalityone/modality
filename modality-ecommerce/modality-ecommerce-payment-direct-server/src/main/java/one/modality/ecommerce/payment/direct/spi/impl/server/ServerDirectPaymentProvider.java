package one.modality.ecommerce.payment.direct.spi.impl.server;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.direct.GetDirectPaymentGatewayInfosArgument;
import one.modality.ecommerce.payment.direct.GetDirectPaymentGatewayInfosResult;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentArgument;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentResult;
import one.modality.ecommerce.payment.direct.spi.DirectPaymentProvider;
import one.modality.ecommerce.payment.gateway.direct.spi.DirectPaymentGatewayProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public class ServerDirectPaymentProvider implements DirectPaymentProvider {
    @Override
    public Future<MakeDirectPaymentResult> makeDirectPayment(MakeDirectPaymentArgument argument) {
        Iterator<DirectPaymentGatewayProvider> it = getDirectPaymentGatewayProviders().iterator();
        if (it.hasNext())
            return it.next().makeDirectPayment(argument);
        return Future.failedFuture(new IllegalStateException("No direct payment gateway found!"));
    }

    @Override
    public Future<GetDirectPaymentGatewayInfosResult> getDirectPaymentGatewayInfos(GetDirectPaymentGatewayInfosArgument argument) {
        return Future.succeededFuture(getDirectPaymentGatewayInfosInstant(argument));
    }

    public GetDirectPaymentGatewayInfosResult getDirectPaymentGatewayInfosInstant(GetDirectPaymentGatewayInfosArgument argument) {
        getDirectPaymentGatewayProviders();
        return null;
    }

    protected ServiceLoader<DirectPaymentGatewayProvider> getDirectPaymentGatewayProviders() {
        return ServiceLoader.load(DirectPaymentGatewayProvider.class);
    }
}
