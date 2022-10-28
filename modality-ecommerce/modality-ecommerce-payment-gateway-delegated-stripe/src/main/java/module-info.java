// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.payment.gateway.delegated.stripe {

    // Direct dependencies modules
    requires modality.ecommerce.payment.delegated;
    requires modality.ecommerce.payment.gateway.delegated;
    requires stripe.java;
    requires webfx.platform.async;

    // Exported packages
    exports one.modality.ecommerce.payment.gateway.delegated.spi.impl.stripe;

    // Provided services
    provides one.modality.ecommerce.payment.gateway.delegated.spi.DelegatedPaymentGatewayProvider with one.modality.ecommerce.payment.gateway.delegated.spi.impl.stripe.StripeDelegatedPaymentGatewayProvider;

}