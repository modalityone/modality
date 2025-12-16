package one.modality.ecommerce.payment.server.gateway;

import dev.webfx.platform.async.Future;

public interface PaymentGateway {

    String getName();

    Future<GatewayInitiatePaymentResult> initiatePayment(GatewayInitiatePaymentArgument argument);

    Future<GatewayCompletePaymentResult> completePayment(GatewayCompletePaymentArgument argument);

}
