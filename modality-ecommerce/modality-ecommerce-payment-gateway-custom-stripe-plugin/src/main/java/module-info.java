// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The SPI payment gateway implementation for making custom payments with Stripe.
 */
module modality.ecommerce.payment.gateway.custom.stripe.plugin {

    // Direct dependencies modules
    requires modality.ecommerce.payment.custom;
    requires modality.ecommerce.payment.gateway.custom;
    requires stripe.java;
    requires webfx.platform.async;
    requires webfx.platform.resource;

    // Exported packages
    exports one.modality.ecommerce.payment.gateway.custom.spi.impl.stripe;

    // Resources packages
    opens one.modality.ecommerce.payment.gateway.custom.spi.impl.stripe;

    // Provided services
    provides one.modality.ecommerce.payment.gateway.custom.spi.CustomPaymentGatewayProvider with one.modality.ecommerce.payment.gateway.custom.spi.impl.stripe.StripeCustomPaymentGatewayProvider;

}