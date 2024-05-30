// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Declares the api payment endpoint address, and implements the data serialisation.
 */
module modality.ecommerce.payment.api.buscall {

    // Direct dependencies modules
    requires modality.ecommerce.payment.api;
    requires webfx.platform.ast;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.com.serial;

    // Exported packages
    exports one.modality.ecommerce.payment.api.buscall;
    exports one.modality.ecommerce.payment.api.buscall.serial;

    // Provided services
    provides dev.webfx.stack.com.bus.call.spi.BusCallEndpoint with one.modality.ecommerce.payment.api.buscall.MakeApiPaymentMethodEndpoint, one.modality.ecommerce.payment.api.buscall.GetApiPaymentGatewayInfosMethodEndpoint;
    provides dev.webfx.stack.com.serial.spi.SerialCodec with one.modality.ecommerce.payment.api.buscall.serial.MakeApiPaymentArgumentSerialCodec, one.modality.ecommerce.payment.api.buscall.serial.MakeApiPaymentResultSerialCodec;

}