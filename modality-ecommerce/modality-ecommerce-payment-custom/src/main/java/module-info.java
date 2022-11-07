// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.payment.custom {

    // Direct dependencies modules
    requires java.base;
    requires transitive webfx.platform.async;
    requires webfx.platform.util;

    // Exported packages
    exports one.modality.ecommerce.payment.custom;
    exports one.modality.ecommerce.payment.custom.spi;

    // Used services
    uses one.modality.ecommerce.payment.custom.spi.CustomPaymentProvider;

}