package one.modality.ecommerce.payment.custom.spi.impl.remote;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.com.bus.call.BusCallService;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentArgument;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentResult;
import one.modality.ecommerce.payment.custom.buscall.CustomPaymentServiceBusAddress;
import one.modality.ecommerce.payment.custom.spi.CustomPaymentProvider;

/**
 * @author Bruno Salmon
 */
public class RemoteCustomPaymentProvider implements CustomPaymentProvider {
  @Override
  public Future<InitiateCustomPaymentResult> initiateCustomPayment(
      InitiateCustomPaymentArgument argument) {
    return BusCallService.call(
        CustomPaymentServiceBusAddress.INITIATE_CUSTOM_PAYMENT_METHOD_ADDRESS, argument);
  }
}
