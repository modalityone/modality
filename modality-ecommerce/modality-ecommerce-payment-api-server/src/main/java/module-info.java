// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The actual server-side implementation of the api payment API, which decides which payment gateway to use and calls it.
 */
module modality.ecommerce.payment.api.server {

    // Direct dependencies modules
    requires modality.ecommerce.payment.api;
    requires modality.ecommerce.payment.api.gateway;
    requires webfx.platform.async;
    requires webfx.platform.service;

    // Exported packages
    exports one.modality.ecommerce.payment.api.spi.impl.server;

    // Used services
    uses one.modality.ecommerce.payment.api.gateway.spi.ApiPaymentGatewayProvider;

    // Provided services
    provides one.modality.ecommerce.payment.api.spi.ApiPaymentProvider with one.modality.ecommerce.payment.api.spi.impl.server.ServerApiPaymentProvider;

}