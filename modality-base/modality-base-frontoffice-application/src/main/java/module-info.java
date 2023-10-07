// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.frontoffice.application {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.application;
    requires webfx.extras.flexbox;
    requires webfx.platform.conf;
    requires webfx.platform.os;
    requires webfx.stack.i18n;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.base.frontoffice.application;

    // Provided services
    provides javafx.application.Application with one.modality.base.frontoffice.application.ModalityFrontOfficeApplication;

}