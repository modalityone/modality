package one.modality.ecommerce.payment.direct.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.payment.direct.DirectPaymentService;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentArgument;
import one.modality.ecommerce.payment.direct.MakeDirectPaymentResult;

/**
 * @author Bruno Salmon
 */
public final class MakeDirectPaymentMethodEndpoint
    extends AsyncFunctionBusCallEndpoint<MakeDirectPaymentArgument, MakeDirectPaymentResult> {

  public MakeDirectPaymentMethodEndpoint() {
    super(
        DirectPaymentServiceBusAddress.MAKE_DIRECT_PAYMENT_METHOD_ADDRESS,
        DirectPaymentService::makeDirectPayment);
  }
}
