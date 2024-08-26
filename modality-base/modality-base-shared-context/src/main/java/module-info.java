// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.shared.context {

    // Direct dependencies modules
    requires webfx.platform.ast;
    requires webfx.stack.com.serial;

    // Exported packages
    exports one.modality.base.shared.context;
    exports one.modality.base.shared.context.serial;

    // Provided services
    provides dev.webfx.stack.com.serial.spi.SerialCodec with one.modality.base.shared.context.serial.ModalityContextSerialCodec;

}