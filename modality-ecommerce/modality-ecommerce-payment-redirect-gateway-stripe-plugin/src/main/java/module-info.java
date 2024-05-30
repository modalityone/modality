// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The SPI payment gateway implementation for making redirect payments with Stripe.
 */
module modality.ecommerce.payment.redirect.gateway.stripe.plugin {

    // Direct dependencies modules
    requires modality.ecommerce.payment.redirect;
    requires modality.ecommerce.payment.redirect.gateway;
    requires stripe.java;
    requires webfx.platform.async;

    // Exported packages
    exports one.modality.ecommerce.payment.redirect.gateway.spi.impl.stripe;

    // Provided services
    provides one.modality.ecommerce.payment.redirect.gateway.spi.RedirectPaymentGatewayProvider with one.modality.ecommerce.payment.redirect.gateway.spi.impl.stripe.StripeRedirectPaymentGatewayProvider;

}