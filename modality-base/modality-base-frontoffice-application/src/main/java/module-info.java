// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.frontoffice.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.application;
    requires modality.base.client.mainframe.dialogarea.fx;
    requires webfx.extras.panes;
    requires webfx.kit.util;
    requires webfx.platform.conf;
    requires webfx.platform.os;
    requires webfx.platform.util;
    requires webfx.stack.i18n;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.base.frontoffice.application;

    // Provided services
    provides javafx.application.Application with one.modality.base.frontoffice.application.ModalityFrontOfficeApplication;

}