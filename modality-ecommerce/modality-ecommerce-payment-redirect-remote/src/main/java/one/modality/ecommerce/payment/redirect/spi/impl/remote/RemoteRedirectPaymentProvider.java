package one.modality.ecommerce.payment.redirect.spi.impl.remote;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.com.bus.call.BusCallService;
import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentArgument;
import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentResult;
import one.modality.ecommerce.payment.redirect.buscall.RedirectPaymentServiceBusAddress;
import one.modality.ecommerce.payment.redirect.spi.RedirectPaymentProvider;

/**
 * @author Bruno Salmon
 */
public class RemoteRedirectPaymentProvider implements RedirectPaymentProvider {
    @Override
    public Future<InitiateRedirectPaymentResult> initiateRedirectPayment(InitiateRedirectPaymentArgument argument) {
        return BusCallService.call(RedirectPaymentServiceBusAddress.INITIATE_REDIRECT_PAYMENT_METHOD_ADDRESS, argument);
    }
}
