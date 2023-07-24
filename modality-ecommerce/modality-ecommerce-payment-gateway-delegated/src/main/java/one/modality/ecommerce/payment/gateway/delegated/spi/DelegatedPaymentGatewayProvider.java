package one.modality.ecommerce.payment.gateway.delegated.spi;

import dev.webfx.platform.async.Future;

import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentResult;

public interface DelegatedPaymentGatewayProvider {

    Future<InitiateDelegatedPaymentResult> initiateDelegatedPayment(
            InitiateDelegatedPaymentGatewayArgument argument);
}
