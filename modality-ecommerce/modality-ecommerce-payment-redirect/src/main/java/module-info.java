// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The logical starting point to initiate redirect payments (this API can be called either on client or server).
 */
module modality.ecommerce.payment.redirect {

    // Direct dependencies modules
    requires transitive webfx.platform.async;
    requires webfx.platform.service;

    // Exported packages
    exports one.modality.ecommerce.payment.redirect;
    exports one.modality.ecommerce.payment.redirect.spi;

    // Used services
    uses one.modality.ecommerce.payment.redirect.spi.RedirectPaymentProvider;

}