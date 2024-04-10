// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The logical starting point to initiate direct payments (this API can be called either on client or server).
 */
module modality.ecommerce.payment.direct {

    // Direct dependencies modules
    requires transitive webfx.platform.async;
    requires webfx.platform.service;

    // Exported packages
    exports one.modality.ecommerce.payment.direct;
    exports one.modality.ecommerce.payment.direct.spi;

    // Used services
    uses one.modality.ecommerce.payment.direct.spi.DirectPaymentProvider;

}