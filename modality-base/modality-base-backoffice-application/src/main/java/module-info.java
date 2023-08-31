// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.graphics;
    requires modality.base.backoffice.gantt.canvas;
    requires modality.base.client.application;
    requires modality.base.client.gantt.fx;
    requires webfx.extras.theme;
    requires webfx.kit.util;
    requires webfx.platform.conf;
    requires webfx.platform.util;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.ui.controls;

    // Exported packages
    exports one.modality.base.backoffice.application;

    // Used services
    uses one.modality.base.backoffice.application.MainFrameHeaderNodeProvider;

    // Provided services
    provides javafx.application.Application with one.modality.base.backoffice.application.ModalityBackOfficeApplication;

}