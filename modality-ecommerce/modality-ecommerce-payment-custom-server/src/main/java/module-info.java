// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The actual server-side implementation of the custom payment API, which decides which payment gateway to use and calls it.
 */
module modality.ecommerce.payment.custom.server {

    // Direct dependencies modules
    requires java.base;
    requires modality.ecommerce.payment.custom;
    requires modality.ecommerce.payment.gateway.custom;
    requires webfx.platform.async;
    requires webfx.platform.util;

    // Exported packages
    exports one.modality.ecommerce.payment.custom.spi.impl.server;

    // Used services
    uses one.modality.ecommerce.payment.gateway.custom.spi.CustomPaymentGatewayProvider;

    // Provided services
    provides one.modality.ecommerce.payment.custom.spi.CustomPaymentProvider with one.modality.ecommerce.payment.custom.spi.impl.server.ServerCustomPaymentProvider;

}