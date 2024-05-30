// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The client-side implementation of the redirect payment API (just forwards the request to the server).
 */
module modality.ecommerce.payment.redirect.remote {

    // Direct dependencies modules
    requires modality.ecommerce.payment.redirect;
    requires modality.ecommerce.payment.redirect.buscall;
    requires webfx.platform.async;
    requires webfx.stack.com.bus.call;

    // Exported packages
    exports one.modality.ecommerce.payment.redirect.spi.impl.remote;

    // Provided services
    provides one.modality.ecommerce.payment.redirect.spi.RedirectPaymentProvider with one.modality.ecommerce.payment.redirect.spi.impl.remote.RemoteRedirectPaymentProvider;

}