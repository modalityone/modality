// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The client-side implementation of the embedded payment API (just forwards the request to the server).
 */
module modality.ecommerce.payment.remote {

    // Direct dependencies modules
    requires modality.ecommerce.payment;
    requires modality.ecommerce.payment.buscall;
    requires webfx.platform.async;
    requires webfx.stack.com.bus.call;

    // Exported packages
    exports one.modality.ecommerce.payment.spi.impl.remote;

    // Provided services
    provides one.modality.ecommerce.payment.spi.PaymentServiceProvider with one.modality.ecommerce.payment.spi.impl.remote.RemotePaymentServiceProvider;

}