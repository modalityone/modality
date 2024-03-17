// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The logical starting point to initiate custom payments (this API can be called either on client or server).
 */
module modality.ecommerce.payment.custom {

    // Direct dependencies modules
    requires transitive webfx.platform.async;
    requires webfx.platform.util;

    // Exported packages
    exports one.modality.ecommerce.payment.custom;
    exports one.modality.ecommerce.payment.custom.spi;

    // Used services
    uses one.modality.ecommerce.payment.custom.spi.CustomPaymentProvider;

}