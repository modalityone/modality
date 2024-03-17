// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The SPI payment gateway implementation for making direct payments with Authorize.net.
 */
module modality.ecommerce.payment.gateway.direct.authorizedotnet.plugin {

    // Direct dependencies modules
    requires anet.java.sdk;
    requires modality.ecommerce.payment.direct;
    requires modality.ecommerce.payment.gateway.direct;
    requires webfx.platform.async;
    requires webfx.platform.scheduler;

    // Exported packages
    exports one.modality.ecommerce.payment.gateway.direct.spi.impl.authorizedotnet;

    // Provided services
    provides one.modality.ecommerce.payment.gateway.direct.spi.DirectPaymentGatewayProvider with one.modality.ecommerce.payment.gateway.direct.spi.impl.authorizedotnet.AuthorizeDotNetDirectPaymentGatewayProvider;

}