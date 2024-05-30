// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The actual server-side implementation of the redirect payment API, which decides which payment gateway to use and calls it.
 */
module modality.ecommerce.payment.redirect.server {

    // Direct dependencies modules
    requires modality.ecommerce.payment.redirect;
    requires modality.ecommerce.payment.redirect.gateway;
    requires webfx.platform.async;
    requires webfx.platform.service;

    // Exported packages
    exports one.modality.ecommerce.payment.redirect.spi.impl.server;

    // Used services
    uses one.modality.ecommerce.payment.redirect.gateway.spi.RedirectPaymentGatewayProvider;

    // Provided services
    provides one.modality.ecommerce.payment.redirect.spi.RedirectPaymentProvider with one.modality.ecommerce.payment.redirect.spi.impl.server.ServerRedirectPaymentProvider;

}