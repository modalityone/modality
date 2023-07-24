package one.modality.ecommerce.payment.custom;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.serviceloader.SingleServiceProvider;
import java.util.ServiceLoader;
import one.modality.ecommerce.payment.custom.spi.CustomPaymentProvider;

/**
 * @author Bruno Salmon
 */
public final class CustomPaymentService {

  public static CustomPaymentProvider getProvider() {
    return SingleServiceProvider.getProvider(
        CustomPaymentProvider.class, () -> ServiceLoader.load(CustomPaymentProvider.class));
  }

  public static Future<InitiateCustomPaymentResult> initiateCustomPayment(
      InitiateCustomPaymentArgument argument) {
    return getProvider().initiateCustomPayment(argument);
  }
}
