package one.modality.ecommerce.payment.gateway.custom.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentArgument;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentResult;

public interface CustomPaymentGatewayProvider {

    Future<InitiateCustomPaymentResult> initiateCustomPayment(InitiateCustomPaymentArgument argument);

}
