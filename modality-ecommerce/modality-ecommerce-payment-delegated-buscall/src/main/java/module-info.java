// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Declares the delegated payment endpoint address, and implements the data serialisation.
 */
module modality.ecommerce.payment.delegated.buscall {

    // Direct dependencies modules
    requires modality.ecommerce.payment.delegated;
    requires webfx.platform.ast.json;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.com.serial;

    // Exported packages
    exports one.modality.ecommerce.payment.delegated.buscall;
    exports one.modality.ecommerce.payment.delegated.buscall.serial;

    // Provided services
    provides dev.webfx.stack.com.bus.call.spi.BusCallEndpoint with one.modality.ecommerce.payment.delegated.buscall.InitiateDelegatedPaymentMethodEndpoint;
    provides dev.webfx.stack.com.serial.spi.SerialCodec with one.modality.ecommerce.payment.delegated.buscall.serial.InitiateDelegatedPaymentArgumentSerialCodec, one.modality.ecommerce.payment.delegated.buscall.serial.InitiateDelegatedPaymentResultSerialCodec;

}