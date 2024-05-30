// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The generic Server Provider Interface for embedded payments to call a payment gateway.
 */
module modality.ecommerce.payment.embedded.gateway {

    // Direct dependencies modules
    requires modality.ecommerce.payment.embedded;
    requires webfx.platform.async;

    // Exported packages
    exports one.modality.ecommerce.payment.embedded.gateway.spi;

}