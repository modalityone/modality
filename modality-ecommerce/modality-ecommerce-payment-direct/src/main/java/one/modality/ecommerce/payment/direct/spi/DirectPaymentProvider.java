package one.modality.ecommerce.payment.direct.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.direct.GetDirectPaymentGatewayInfosArgument;
import one.modality.ecommerce.payment.direct.GetDirectPaymentGatewayInfosResult;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentArgument;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentResult;

/**
 * @author Bruno Salmon
 */
public interface DirectPaymentProvider {

  Future<MakeDirectPaymentResult> makeDirectPayment(MakeDirectPaymentArgument argument);

  Future<GetDirectPaymentGatewayInfosResult> getDirectPaymentGatewayInfos(
      GetDirectPaymentGatewayInfosArgument argument);
}
