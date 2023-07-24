// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.shared.authn {

    // Direct dependencies modules
    requires java.base;
    requires webfx.platform.json;
    requires webfx.stack.com.serial;

    // Exported packages
    exports one.modality.crm.shared.services.authn;
    exports one.modality.crm.shared.services.authn.serial;

    // Provided services
    provides dev.webfx.stack.com.serial.spi.SerialCodec with
            one.modality.crm.shared.services.authn.serial.ModalityUserPrincipalSerialCodec;
}
