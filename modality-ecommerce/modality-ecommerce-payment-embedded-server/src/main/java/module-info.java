// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The actual server-side implementation of the embedded payment API, which decides which payment gateway to use and calls it.
 */
module modality.ecommerce.payment.embedded.server {

    // Direct dependencies modules
    requires modality.ecommerce.payment.embedded;
    requires modality.ecommerce.payment.embedded.gateway;
    requires webfx.platform.async;
    requires webfx.platform.service;

    // Exported packages
    exports one.modality.ecommerce.payment.embedded.spi.impl.server;

    // Used services
    uses one.modality.ecommerce.payment.embedded.gateway.spi.EmbeddedPaymentGatewayProvider;

    // Provided services
    provides one.modality.ecommerce.payment.embedded.spi.EmbeddedPaymentProvider with one.modality.ecommerce.payment.embedded.spi.impl.server.ServerEmbeddedPaymentProvider;

}