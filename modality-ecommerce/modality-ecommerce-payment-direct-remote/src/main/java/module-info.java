// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The client-side implementation of the direct payment API (just forwards the request to the server).
 */
module modality.ecommerce.payment.direct.remote {

    // Direct dependencies modules
    requires modality.ecommerce.payment.direct;
    requires modality.ecommerce.payment.direct.buscall;
    requires webfx.platform.async;
    requires webfx.stack.com.bus.call;

    // Exported packages
    exports one.modality.ecommerce.payment.direct.spi.impl.remote;

    // Provided services
    provides one.modality.ecommerce.payment.direct.spi.DirectPaymentProvider with one.modality.ecommerce.payment.direct.spi.impl.remote.RemoteDirectPaymentProvider;

}