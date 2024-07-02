// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The logical starting point to initiate embedded payments (this API can be called either on client or server).
 */
module modality.ecommerce.payment {

    // Direct dependencies modules
    requires transitive webfx.platform.async;
    requires webfx.platform.service;
    requires webfx.platform.useragent;

    // Exported packages
    exports one.modality.ecommerce.payment;
    exports one.modality.ecommerce.payment.spi;

    // Used services
    uses one.modality.ecommerce.payment.spi.PaymentServiceProvider;

}