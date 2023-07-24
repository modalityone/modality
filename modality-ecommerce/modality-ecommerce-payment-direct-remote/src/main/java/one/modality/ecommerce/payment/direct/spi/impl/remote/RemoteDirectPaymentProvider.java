package one.modality.ecommerce.payment.direct.spi.impl.remote;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.com.bus.call.BusCallService;
import one.modality.ecommerce.payment.direct.GetDirectPaymentGatewayInfosArgument;
import one.modality.ecommerce.payment.direct.GetDirectPaymentGatewayInfosResult;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentArgument;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentResult;
import one.modality.ecommerce.payment.direct.buscall.DirectPaymentServiceBusAddress;
import one.modality.ecommerce.payment.direct.spi.DirectPaymentProvider;

/**
 * @author Bruno Salmon
 */
public class RemoteDirectPaymentProvider implements DirectPaymentProvider {

  @Override
  public Future<MakeDirectPaymentResult> makeDirectPayment(MakeDirectPaymentArgument argument) {
    return BusCallService.call(
        DirectPaymentServiceBusAddress.MAKE_DIRECT_PAYMENT_METHOD_ADDRESS, argument);
  }

  @Override
  public Future<GetDirectPaymentGatewayInfosResult> getDirectPaymentGatewayInfos(
      GetDirectPaymentGatewayInfosArgument argument) {
    return BusCallService.call(
        DirectPaymentServiceBusAddress.GET_DIRECT_PAYMENT_GATEWAY_INFOS_METHOD_ADDRESS, argument);
  }
}
