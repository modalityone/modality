package one.modality.ecommerce.payment.delegated;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.serviceloader.SingleServiceProvider;
import java.util.ServiceLoader;
import one.modality.ecommerce.payment.delegated.spi.DelegatedPaymentProvider;

/**
 * @author Bruno Salmon
 */
public class DelegatedPaymentService {

  public static DelegatedPaymentProvider getProvider() {
    return SingleServiceProvider.getProvider(
        DelegatedPaymentProvider.class, () -> ServiceLoader.load(DelegatedPaymentProvider.class));
  }

  public static Future<InitiateDelegatedPaymentResult> initiateDelegatedPayment(
      InitiateDelegatedPaymentArgument argument) {
    return getProvider().initiateDelegatedPayment(argument);
  }
}
