package one.modality.ecommerce.payment.redirect.gateway.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentResult;


public interface RedirectPaymentGatewayProvider {

    Future<InitiateRedirectPaymentResult> initiateRedirectPayment(InitiateRedirectPaymentGatewayArgument argument);

}
