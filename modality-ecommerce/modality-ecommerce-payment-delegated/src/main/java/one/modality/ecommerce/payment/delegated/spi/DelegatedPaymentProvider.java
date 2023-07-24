package one.modality.ecommerce.payment.delegated.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentArgument;
import one.modality.ecommerce.payment.delegated.InitiateDelegatedPaymentResult;

public interface DelegatedPaymentProvider {

  Future<InitiateDelegatedPaymentResult> initiateDelegatedPayment(
      InitiateDelegatedPaymentArgument argument);
}
