// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The actual server-side implementation of the delegated payment API, which decides which payment gateway to use and calls it.
 */
module modality.ecommerce.payment.delegated.server {

    // Direct dependencies modules
    requires java.base;
    requires modality.ecommerce.payment.delegated;
    requires modality.ecommerce.payment.gateway.delegated;
    requires webfx.platform.async;
    requires webfx.platform.util;

    // Exported packages
    exports one.modality.ecommerce.payment.delegated.spi.impl.server;

    // Used services
    uses one.modality.ecommerce.payment.gateway.delegated.spi.DelegatedPaymentGatewayProvider;

    // Provided services
    provides one.modality.ecommerce.payment.delegated.spi.DelegatedPaymentProvider with one.modality.ecommerce.payment.delegated.spi.impl.server.ServerDelegatedPaymentProvider;

}