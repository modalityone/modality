// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.payment.direct {

    // Direct dependencies modules
    requires java.base;
    requires transitive webfx.platform.async;
    requires webfx.platform.util;

    // Exported packages
    exports one.modality.ecommerce.payment.direct;
    exports one.modality.ecommerce.payment.direct.spi;

    // Used services
    uses one.modality.ecommerce.payment.direct.spi.DirectPaymentProvider;
}
