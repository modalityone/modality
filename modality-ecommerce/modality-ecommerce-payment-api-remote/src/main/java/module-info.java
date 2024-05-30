// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The client-side implementation of the api payment API (just forwards the request to the server).
 */
module modality.ecommerce.payment.api.remote {

    // Direct dependencies modules
    requires modality.ecommerce.payment.api;
    requires modality.ecommerce.payment.api.buscall;
    requires webfx.platform.async;
    requires webfx.stack.com.bus.call;

    // Exported packages
    exports one.modality.ecommerce.payment.api.spi.impl.remote;

    // Provided services
    provides one.modality.ecommerce.payment.api.spi.ApiPaymentProvider with one.modality.ecommerce.payment.api.spi.impl.remote.RemoteApiPaymentProvider;

}