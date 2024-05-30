// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The SPI payment gateway implementation for making embedded payments with Stripe.
 */
module modality.ecommerce.payment.gateway.embedded.stripe.plugin {

    // Direct dependencies modules
    requires modality.ecommerce.payment.embedded;
    requires modality.ecommerce.payment.gateway.embedded;
    requires stripe.java;
    requires webfx.platform.async;
    requires webfx.platform.resource;

    // Exported packages
    exports one.modality.ecommerce.payment.gateway.embedded.spi.impl.stripe;

    // Resources packages
    opens one.modality.ecommerce.payment.gateway.embedded.spi.impl.stripe;

    // Provided services
    provides one.modality.ecommerce.payment.gateway.embedded.spi.EmbeddedPaymentGatewayProvider with one.modality.ecommerce.payment.gateway.embedded.spi.impl.stripe.StripeEmbeddedPaymentGatewayProvider;

}