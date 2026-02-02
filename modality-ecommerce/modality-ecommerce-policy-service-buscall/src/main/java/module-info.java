// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.policy.service.buscall {

    // Direct dependencies modules
    requires modality.ecommerce.policy.service;
    requires webfx.platform.ast;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.com.serial;
    requires webfx.stack.db.query;

    // Exported packages
    exports one.modality.ecommerce.policy.service.buscall;
    exports one.modality.ecommerce.policy.service.buscall.serial;

    // Provided services
    provides dev.webfx.stack.com.bus.call.spi.BusCallEndpoint with one.modality.ecommerce.policy.service.buscall.LoadPolicyMethodEndpoint, one.modality.ecommerce.policy.service.buscall.LoadAvailabilitiesMethodEndpoint;
    provides dev.webfx.stack.com.serial.spi.SerialCodec with one.modality.ecommerce.policy.service.buscall.serial.LoadPolicyArgumentSerialCodec, one.modality.ecommerce.policy.service.buscall.serial.PolicyAggregateSerialCodec;

}