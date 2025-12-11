// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.frontoffice.application {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.client.application;
    requires modality.base.frontoffice.mainframe.activity;
    requires modality.base.frontoffice.utility;
    requires webfx.platform.conf;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.base.frontoffice.application;

    // Provided services
    provides javafx.application.Application with one.modality.base.frontoffice.application.ModalityFrontOfficeApplication;

}