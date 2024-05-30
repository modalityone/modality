package one.modality.ecommerce.payment.embedded.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentArgument;
import one.modality.ecommerce.payment.embedded.InitiateEmbeddedPaymentResult;

public interface EmbeddedPaymentProvider {

    Future<InitiateEmbeddedPaymentResult> initiateEmbeddedPayment(InitiateEmbeddedPaymentArgument argument);

}
