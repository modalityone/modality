// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Declares the redirect payment endpoint address, and implements the data serialisation.
 */
module modality.ecommerce.payment.redirect.buscall {

    // Direct dependencies modules
    requires modality.ecommerce.payment.redirect;
    requires webfx.platform.ast;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.com.serial;

    // Exported packages
    exports one.modality.ecommerce.payment.redirect.buscall;
    exports one.modality.ecommerce.payment.redirect.buscall.serial;

    // Provided services
    provides dev.webfx.stack.com.bus.call.spi.BusCallEndpoint with one.modality.ecommerce.payment.redirect.buscall.InitiateRedirectPaymentMethodEndpoint;
    provides dev.webfx.stack.com.serial.spi.SerialCodec with one.modality.ecommerce.payment.redirect.buscall.serial.InitiateRedirectPaymentArgumentSerialCodec, one.modality.ecommerce.payment.redirect.buscall.serial.InitiateRedirectPaymentResultSerialCodec;

}