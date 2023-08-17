// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The generic Server Provider Interface for custom payments to call a payment gateway.
 */
module modality.ecommerce.payment.gateway.custom {

    // Direct dependencies modules
    requires modality.ecommerce.payment.custom;
    requires webfx.platform.async;

    // Exported packages
    exports one.modality.ecommerce.payment.gateway.custom.spi;

}