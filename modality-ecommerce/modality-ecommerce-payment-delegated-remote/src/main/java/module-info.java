// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.payment.delegated.remote {

    // Direct dependencies modules
    requires modality.ecommerce.payment.delegated;
    requires webfx.platform.async;
    requires webfx.stack.com.bus.call;

    // Exported packages
    exports one.modality.ecommerce.payment.delegated.spi.impl.remote;

    // Provided services
    provides one.modality.ecommerce.payment.delegated.spi.DelegatedPaymentProvider with one.modality.ecommerce.payment.delegated.spi.impl.remote.RemoteDelegatedPaymentProvider;

}