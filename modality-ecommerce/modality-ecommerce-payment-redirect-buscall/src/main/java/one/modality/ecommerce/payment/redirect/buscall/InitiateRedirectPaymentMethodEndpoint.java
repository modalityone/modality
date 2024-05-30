package one.modality.ecommerce.payment.redirect.buscall;

import dev.webfx.stack.com.bus.call.spi.AsyncFunctionBusCallEndpoint;
import one.modality.ecommerce.payment.redirect.RedirectPaymentService;
import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentArgument;
import one.modality.ecommerce.payment.redirect.InitiateRedirectPaymentResult;

/**
 * @author Bruno Salmon
 */
public final class InitiateRedirectPaymentMethodEndpoint extends AsyncFunctionBusCallEndpoint<InitiateRedirectPaymentArgument, InitiateRedirectPaymentResult> {

    public InitiateRedirectPaymentMethodEndpoint() {
        super(RedirectPaymentServiceBusAddress.INITIATE_REDIRECT_PAYMENT_METHOD_ADDRESS, RedirectPaymentService::initiateRedirectPayment);
    }

}
