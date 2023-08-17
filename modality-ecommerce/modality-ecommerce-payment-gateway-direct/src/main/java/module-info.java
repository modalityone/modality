// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The generic Server Provider Interface for custom payments to call a payment gateway.
 */
module modality.ecommerce.payment.gateway.direct {

    // Direct dependencies modules
    requires modality.ecommerce.payment.direct;
    requires webfx.platform.async;

    // Exported packages
    exports one.modality.ecommerce.payment.gateway.direct.spi;

}