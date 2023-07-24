// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.operationactionsloading {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires webfx.platform.boot;
    requires webfx.platform.console;
    requires webfx.stack.authz.client;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.entity;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.base.client.operationactionsloading;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationModuleBooter with
            one.modality.base.client.operationactionsloading.ModalityClientOperationActionsLoader;
}
