package one.modality.ecommerce.payment.gateway.direct.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentArgument;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentResult;

/**
 * @author Bruno Salmon
 */
public interface DirectPaymentGatewayProvider {

  Future<MakeDirectPaymentResult> makeDirectPayment(MakeDirectPaymentArgument argument);
}
