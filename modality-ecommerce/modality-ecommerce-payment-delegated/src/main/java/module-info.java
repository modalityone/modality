// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The logical starting point to initiate delegated payments (this API can be called either on client or server).
 */
module modality.ecommerce.payment.delegated {

    // Direct dependencies modules
    requires transitive webfx.platform.async;
    requires webfx.platform.service;

    // Exported packages
    exports one.modality.ecommerce.payment.delegated;
    exports one.modality.ecommerce.payment.delegated.spi;

    // Used services
    uses one.modality.ecommerce.payment.delegated.spi.DelegatedPaymentProvider;

}