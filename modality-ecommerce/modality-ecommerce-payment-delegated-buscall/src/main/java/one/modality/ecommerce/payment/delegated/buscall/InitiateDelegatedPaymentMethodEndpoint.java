package one.modality.ecommerce.payment.delegated.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.payment.delegated.DelegatedPaymentService;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentArgument;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentResult;

/**
 * @author Bruno Salmon
 */
public final class InitiateDelegatedPaymentMethodEndpoint
    extends AsyncFunctionBusCallEndpoint<
        InitiateDelegatedPaymentArgument, InitiateDelegatedPaymentResult> {

  public InitiateDelegatedPaymentMethodEndpoint() {
    super(
        DelegatedPaymentServiceBusAddress.INITIATE_DELEGATED_PAYMENT_METHOD_ADDRESS,
        DelegatedPaymentService::initiateDelegatedPayment);
  }
}
