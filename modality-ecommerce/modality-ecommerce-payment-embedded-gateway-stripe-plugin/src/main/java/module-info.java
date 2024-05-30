// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The SPI payment gateway implementation for making embedded payments with Stripe.
 */
module modality.ecommerce.payment.embedded.gateway.stripe.plugin {

    // Direct dependencies modules
    requires modality.ecommerce.payment.embedded;
    requires modality.ecommerce.payment.embedded.gateway;
    requires stripe.java;
    requires webfx.platform.async;
    requires webfx.platform.resource;

    // Exported packages
    exports one.modality.ecommerce.payment.embedded.gateway.spi.impl.stripe;

    // Resources packages
    opens one.modality.ecommerce.payment.embedded.gateway.spi.impl.stripe;

    // Provided services
    provides one.modality.ecommerce.payment.embedded.gateway.spi.EmbeddedPaymentGatewayProvider with one.modality.ecommerce.payment.embedded.gateway.spi.impl.stripe.StripeEmbeddedPaymentGatewayProvider;

}