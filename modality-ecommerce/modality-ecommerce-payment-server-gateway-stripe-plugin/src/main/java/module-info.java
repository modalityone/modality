// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The SPI payment gateway implementation for making embedded payments with Stripe.
 */
module modality.ecommerce.payment.server.gateway.stripe.plugin {

    // Direct dependencies modules
    requires modality.ecommerce.payment;
    requires modality.ecommerce.payment.server.gateway;
    requires stripe.java;
    requires webfx.platform.async;
    requires webfx.platform.resource;

    // Exported packages
    exports one.modality.ecommerce.payment.server.gateway.impl.stripe;

    // Resources packages
    opens one.modality.ecommerce.payment.server.gateway.impl.stripe;

    // Provided services
    provides one.modality.ecommerce.payment.server.gateway.PaymentGateway with one.modality.ecommerce.payment.server.gateway.impl.stripe.StripePaymentGateway;

}