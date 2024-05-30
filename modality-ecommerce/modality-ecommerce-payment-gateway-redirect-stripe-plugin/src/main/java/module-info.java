// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The SPI payment gateway implementation for making redirect payments with Stripe.
 */
module modality.ecommerce.payment.gateway.redirect.stripe.plugin {

    // Direct dependencies modules
    requires modality.ecommerce.payment.gateway.redirect;
    requires modality.ecommerce.payment.redirect;
    requires stripe.java;
    requires webfx.platform.async;

    // Exported packages
    exports one.modality.ecommerce.payment.gateway.redirect.spi.impl.stripe;

    // Provided services
    provides one.modality.ecommerce.payment.gateway.redirect.spi.RedirectPaymentGatewayProvider with one.modality.ecommerce.payment.gateway.redirect.spi.impl.stripe.StripeRedirectPaymentGatewayProvider;

}