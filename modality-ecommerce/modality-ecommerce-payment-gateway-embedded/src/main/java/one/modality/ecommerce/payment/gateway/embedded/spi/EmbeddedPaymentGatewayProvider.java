package one.modality.ecommerce.payment.gateway.embedded.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentArgument;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentResult;

public interface EmbeddedPaymentGatewayProvider {

    Future<InitiateEmbeddedPaymentResult> initiateEmbeddedPayment(InitiateEmbeddedPaymentArgument argument);

}
