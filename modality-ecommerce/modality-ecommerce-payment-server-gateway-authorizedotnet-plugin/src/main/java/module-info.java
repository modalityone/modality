// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The SPI payment gateway implementation for making api payments with Authorize.net.
 */
module modality.ecommerce.payment.server.gateway.authorizedotnet.plugin {

    // Direct dependencies modules
    requires anet.java.sdk;
    requires modality.ecommerce.payment.server.gateway;
    requires webfx.platform.async;
    requires webfx.platform.scheduler;

    // Exported packages
    exports one.modality.ecommerce.payment.server.gateway.impl.authorizedotnet;

    // Provided services
    provides one.modality.ecommerce.payment.server.gateway.PaymentGateway with one.modality.ecommerce.payment.server.gateway.impl.authorizedotnet.AuthorizeDotNetApiPaymentGateway;

}