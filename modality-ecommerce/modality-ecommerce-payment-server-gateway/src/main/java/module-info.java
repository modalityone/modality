// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The generic Server Provider Interface for embedded payments to call a payment gateway.
 */
module modality.ecommerce.payment.server.gateway {

    // Direct dependencies modules
    requires modality.ecommerce.payment;
    requires webfx.platform.async;

    // Exported packages
    exports one.modality.ecommerce.payment.server.gateway;
    exports one.modality.ecommerce.payment.server.gateway.impl.util;

}