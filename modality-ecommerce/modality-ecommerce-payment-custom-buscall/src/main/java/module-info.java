// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.payment.custom.buscall {

    // Direct dependencies modules
    requires modality.ecommerce.payment.custom;
    requires webfx.platform.json;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.com.serial;

    // Exported packages
    exports one.modality.ecommerce.payment.custom.buscall;
    exports one.modality.ecommerce.payment.custom.buscall.serial;

    // Provided services
    provides dev.webfx.stack.com.bus.call.spi.BusCallEndpoint with
            one.modality.ecommerce.payment.custom.buscall.InitiateCustomPaymentMethodEndpoint;
    provides dev.webfx.stack.com.serial.spi.SerialCodec with
            one.modality.ecommerce.payment.custom.buscall.serial
                    .InitiateCustomPaymentArgumentSerialCodec,
            one.modality.ecommerce.payment.custom.buscall.serial
                    .InitiateCustomPaymentResultSerialCodec;
}
