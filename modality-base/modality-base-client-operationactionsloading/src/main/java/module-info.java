// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.operationactionsloading {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires webfx.platform.async;
    requires webfx.platform.boot;
    requires webfx.platform.console;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.entity;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports org.modality_project.base.client.operationactionsloading;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationModuleBooter with org.modality_project.base.client.operationactionsloading.ModalityClientOperationActionsLoader;

}