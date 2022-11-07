// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.payment.custom.server {

    // Direct dependencies modules
    requires java.base;
    requires modality.ecommerce.payment.custom;
    requires modality.ecommerce.payment.gateway.custom;
    requires webfx.platform.async;

    // Exported packages
    exports one.modality.ecommerce.payment.custom.spi.impl.server;

    // Used services
    uses one.modality.ecommerce.payment.gateway.custom.spi.CustomPaymentGatewayProvider;

    // Provided services
    provides one.modality.ecommerce.payment.custom.spi.CustomPaymentProvider with one.modality.ecommerce.payment.custom.spi.impl.server.ServerCustomPaymentProvider;

}