// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The generic Server Provider Interface for delegated payments to call a payment gateway.
 */
module modality.ecommerce.payment.gateway.delegated {

    // Direct dependencies modules
    requires modality.ecommerce.payment.delegated;
    requires webfx.platform.async;

    // Exported packages
    exports one.modality.ecommerce.payment.gateway.delegated.spi;

}