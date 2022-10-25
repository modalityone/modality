// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.payment.direct {

    // Direct dependencies modules
    requires java.base;
    requires webfx.platform.async;
    requires webfx.platform.json;
    requires webfx.platform.util;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.com.serial;

    // Exported packages
    exports one.modality.ecommerce.payment.direct;
    exports one.modality.ecommerce.payment.direct.buscall;
    exports one.modality.ecommerce.payment.direct.spi;

    // Used services
    uses one.modality.ecommerce.payment.direct.spi.DirectPaymentProvider;

    // Provided services
    provides dev.webfx.stack.com.bus.call.spi.BusCallEndpoint with one.modality.ecommerce.payment.direct.buscall.MakeDirectPaymentMethodEndpoint, one.modality.ecommerce.payment.direct.buscall.GetDirectPaymentGatewayInfosMethodEndpoint;
    provides dev.webfx.stack.com.serial.spi.SerialCodec with one.modality.ecommerce.payment.direct.MakeDirectPaymentArgument.ProvidedSerialCodec, one.modality.ecommerce.payment.direct.MakeDirectPaymentResult.ProvidedSerialCodec;

}