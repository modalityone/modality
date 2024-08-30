// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Declares the embedded payment endpoint address, and implements the data serialisation.
 */
module modality.ecommerce.payment.buscall {

    // Direct dependencies modules
    requires modality.ecommerce.payment;
    requires webfx.platform.ast;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.com.serial;

    // Exported packages
    exports one.modality.ecommerce.payment.buscall;
    exports one.modality.ecommerce.payment.buscall.serial;

    // Provided services
    provides dev.webfx.stack.com.bus.call.spi.BusCallEndpoint with one.modality.ecommerce.payment.buscall.InitiatePaymentMethodEndpoint, one.modality.ecommerce.payment.buscall.CompletePaymentMethodEndpoint, one.modality.ecommerce.payment.buscall.CancelPaymentMethodEndpoint, one.modality.ecommerce.payment.buscall.MakeApiPaymentMethodEndpoint;
    provides dev.webfx.stack.com.serial.spi.SerialCodec with one.modality.ecommerce.payment.buscall.serial.InitiatePaymentArgumentSerialCodec, one.modality.ecommerce.payment.buscall.serial.InitiatePaymentResultSerialCodec, one.modality.ecommerce.payment.buscall.serial.CompletePaymentArgumentSerialCodec, one.modality.ecommerce.payment.buscall.serial.CompletePaymentResultSerialCodec, one.modality.ecommerce.payment.buscall.serial.CancelPaymentArgumentSerialCodec, one.modality.ecommerce.payment.buscall.serial.CancelPaymentResultSerialCodec, one.modality.ecommerce.payment.buscall.serial.MakeApiPaymentArgumentSerialCodec, one.modality.ecommerce.payment.buscall.serial.MakeApiPaymentResultSerialCodec, one.modality.ecommerce.payment.buscall.serial.SandboxCardSerialCodec;

}