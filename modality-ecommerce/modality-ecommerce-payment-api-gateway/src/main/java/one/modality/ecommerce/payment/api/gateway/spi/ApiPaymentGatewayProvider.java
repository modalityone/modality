package one.modality.ecommerce.payment.api.gateway.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.api.MakeApiPaymentArgument;
import one.modality.ecommerce.payment.api.MakeApiPaymentResult;

/**
 * @author Bruno Salmon
 */
public interface ApiPaymentGatewayProvider {

    Future<MakeApiPaymentResult> makeApiPayment(MakeApiPaymentArgument argument);

}
