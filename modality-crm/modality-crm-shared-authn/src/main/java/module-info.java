// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Defines the authentication data (ModalityUserPrincipal) sent by the server to the client over the network.
 */
module modality.crm.shared.authn {

    // Direct dependencies modules
    requires webfx.platform.ast;
    requires webfx.stack.com.serial;

    // Exported packages
    exports one.modality.crm.shared.services.authn;
    exports one.modality.crm.shared.services.authn.serial;

    // Provided services
    provides dev.webfx.stack.com.serial.spi.SerialCodec with one.modality.crm.shared.services.authn.serial.ModalityUserPrincipalSerialCodec, one.modality.crm.shared.services.authn.serial.ModalityGuestPrincipalSerialCodec;

}