// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The SPI payment gateway implementation for making api payments with Authorize.net.
 */
module modality.ecommerce.payment.api.gateway.authorizedotnet.plugin {

    // Direct dependencies modules
    requires anet.java.sdk;
    requires modality.ecommerce.payment.api;
    requires modality.ecommerce.payment.api.gateway;
    requires webfx.platform.async;
    requires webfx.platform.scheduler;

    // Exported packages
    exports one.modality.ecommerce.payment.api.gateway.spi.impl.authorizedotnet;

    // Provided services
    provides one.modality.ecommerce.payment.api.gateway.spi.ApiPaymentGatewayProvider with one.modality.ecommerce.payment.api.gateway.spi.impl.authorizedotnet.AuthorizeDotNetApiPaymentGatewayProvider;

}