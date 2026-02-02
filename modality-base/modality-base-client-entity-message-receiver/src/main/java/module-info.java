// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.entity.message.receiver {

    // Direct dependencies modules
    requires modality.base.shared.entity.message.bus;
    requires transitive webfx.stack.orm.entity.message.receiver;

    // Exported packages
    exports one.modality.base.client.message.receiver;

}