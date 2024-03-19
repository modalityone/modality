// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The actual server-side implementation of the direct payment API, which decides which payment gateway to use and calls it.
 */
module modality.ecommerce.payment.direct.server {

    // Direct dependencies modules
    requires modality.ecommerce.payment.direct;
    requires modality.ecommerce.payment.gateway.direct;
    requires webfx.platform.async;
    requires webfx.platform.util;

    // Exported packages
    exports one.modality.ecommerce.payment.direct.spi.impl.server;

    // Used services
    uses one.modality.ecommerce.payment.gateway.direct.spi.DirectPaymentGatewayProvider;

    // Provided services
    provides one.modality.ecommerce.payment.direct.spi.DirectPaymentProvider with one.modality.ecommerce.payment.direct.spi.impl.server.ServerDirectPaymentProvider;

}