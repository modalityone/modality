// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.all.frontoffice.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.application;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.ui.action;

    // Exported packages
    exports one.modality.all.frontoffice.application;

    // Provided services
    provides javafx.application.Application with one.modality.all.frontoffice.application.ModalityFrontOfficeApplication;

}