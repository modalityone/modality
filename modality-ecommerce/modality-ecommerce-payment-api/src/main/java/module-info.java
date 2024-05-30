// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The logical starting point to initiate api payments (this API can be called either on client or server).
 */
module modality.ecommerce.payment.api {

    // Direct dependencies modules
    requires transitive webfx.platform.async;
    requires webfx.platform.service;

    // Exported packages
    exports one.modality.ecommerce.payment.api;
    exports one.modality.ecommerce.payment.api.spi;

    // Used services
    uses one.modality.ecommerce.payment.api.spi.ApiPaymentProvider;

}