// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.profile.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.profile.fx;
    requires webfx.kit.util;
    requires webfx.platform.boot;
    requires webfx.stack.authn.logout.client;
    requires webfx.stack.session.state.client.fx;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.crm.client.profile;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.crm.client.profile.ModalityClientProfileInitJob;

}