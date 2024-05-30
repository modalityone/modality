package one.modality.ecommerce.payment.redirect.spi;

import dev.webfx.platform.async.Future;
import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentArgument;
import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentResult;


public interface RedirectPaymentProvider {

    Future<InitiateRedirectPaymentResult> initiateRedirectPayment(InitiateRedirectPaymentArgument argument);

}
