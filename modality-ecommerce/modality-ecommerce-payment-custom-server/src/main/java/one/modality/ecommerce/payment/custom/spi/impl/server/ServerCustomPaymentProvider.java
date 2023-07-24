package one.modality.ecommerce.payment.custom.spi.impl.server;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.serviceloader.MultipleServiceProviders;
import java.util.List;
import java.util.ServiceLoader;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentArgument;
import one.modality.ecommerce.payment.custom.InitiateCustomPaymentResult;
import one.modality.ecommerce.payment.custom.spi.CustomPaymentProvider;
import one.modality.ecommerce.payment.gateway.custom.spi.CustomPaymentGatewayProvider;

/**
 * @author Bruno Salmon
 */
public class ServerCustomPaymentProvider implements CustomPaymentProvider {

  private static List<CustomPaymentGatewayProvider> getProviders() {
    return MultipleServiceProviders.getProviders(
        CustomPaymentGatewayProvider.class,
        () -> ServiceLoader.load(CustomPaymentGatewayProvider.class));
  }

  @Override
  public Future<InitiateCustomPaymentResult> initiateCustomPayment(
      InitiateCustomPaymentArgument argument) {
    List<CustomPaymentGatewayProvider> providers = getProviders();
    if (providers.isEmpty())
      return Future.failedFuture(new IllegalStateException("No delegated payment gateway found!"));
    return providers
        .get(0) // Temporary
        .initiateCustomPayment(argument);
  }
}
