// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Declares the embedded payment endpoint address, and implements the data serialisation.
 */
module modality.ecommerce.payment.embedded.buscall {

    // Direct dependencies modules
    requires modality.ecommerce.payment.embedded;
    requires webfx.platform.ast;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.com.serial;

    // Exported packages
    exports one.modality.ecommerce.payment.embedded.buscall;
    exports one.modality.ecommerce.payment.embedded.buscall.serial;

    // Provided services
    provides dev.webfx.stack.com.bus.call.spi.BusCallEndpoint with one.modality.ecommerce.payment.embedded.buscall.InitiateEmbeddedPaymentMethodEndpoint;
    provides dev.webfx.stack.com.serial.spi.SerialCodec with one.modality.ecommerce.payment.embedded.buscall.serial.InitiateEmbeddedPaymentArgumentSerialCodec, one.modality.ecommerce.payment.embedded.buscall.serial.InitiateEmbeddedPaymentResultSerialCodec;

}