// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The generic Server Provider Interface for redirect payments to call a payment gateway.
 */
module modality.ecommerce.payment.redirect.gateway {

    // Direct dependencies modules
    requires modality.ecommerce.payment.redirect;
    requires webfx.platform.async;

    // Exported packages
    exports one.modality.ecommerce.payment.redirect.gateway.spi;

}