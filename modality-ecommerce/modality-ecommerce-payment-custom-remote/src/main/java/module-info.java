// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.payment.custom.remote {

    // Direct dependencies modules
    requires modality.ecommerce.payment.custom;
    requires modality.ecommerce.payment.custom.buscall;
    requires webfx.platform.async;
    requires webfx.stack.com.bus.call;

    // Exported packages
    exports one.modality.ecommerce.payment.custom.spi.impl.remote;

    // Provided services
    provides one.modality.ecommerce.payment.custom.spi.CustomPaymentProvider with
            one.modality.ecommerce.payment.custom.spi.impl.remote.RemoteCustomPaymentProvider;
}
