// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Declares the direct payment endpoint address, and implements the data serialisation.
 */
module modality.ecommerce.payment.direct.buscall {

    // Direct dependencies modules
    requires modality.ecommerce.payment.direct;
    requires webfx.platform.ast.json;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.com.serial;

    // Exported packages
    exports one.modality.ecommerce.payment.direct.buscall;
    exports one.modality.ecommerce.payment.direct.buscall.serial;

    // Provided services
    provides dev.webfx.stack.com.bus.call.spi.BusCallEndpoint with one.modality.ecommerce.payment.direct.buscall.MakeDirectPaymentMethodEndpoint, one.modality.ecommerce.payment.direct.buscall.GetDirectPaymentGatewayInfosMethodEndpoint;
    provides dev.webfx.stack.com.serial.spi.SerialCodec with one.modality.ecommerce.payment.direct.buscall.serial.MakeDirectPaymentArgumentSerialCodec, one.modality.ecommerce.payment.direct.buscall.serial.MakeDirectPaymentResultSerialCodec;

}