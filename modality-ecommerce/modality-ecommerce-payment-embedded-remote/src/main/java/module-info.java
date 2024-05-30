// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The client-side implementation of the embedded payment API (just forwards the request to the server).
 */
module modality.ecommerce.payment.embedded.remote {

    // Direct dependencies modules
    requires modality.ecommerce.payment.embedded;
    requires modality.ecommerce.payment.embedded.buscall;
    requires webfx.platform.async;
    requires webfx.stack.com.bus.call;

    // Exported packages
    exports one.modality.ecommerce.payment.embedded.spi.impl.remote;

    // Provided services
    provides one.modality.ecommerce.payment.embedded.spi.EmbeddedPaymentProvider with one.modality.ecommerce.payment.embedded.spi.impl.remote.RemoteEmbeddedPaymentProvider;

}