// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.all.frontoffice.application {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.application;
    requires webfx.extras.flexbox;
    requires webfx.kit.statusbar;
    requires webfx.platform.os;
    requires webfx.platform.uischeduler;
    requires webfx.stack.ui.action;

    // Exported packages
    exports one.modality.all.frontoffice.application;

    // Provided services
    provides javafx.application.Application with one.modality.all.frontoffice.application.ModalityFrontOfficeApplication;

}