// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.operationactionsloading {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires webfx.framework.client.action;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.operationaction;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.orm.datasourcemodelservice;
    requires webfx.framework.shared.orm.entity;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.boot;
    requires webfx.platform.shared.log;

    // Exported packages
    exports org.modality_project.base.client.operationactionsloading;

    // Provided services
    provides dev.webfx.platform.shared.services.boot.spi.ApplicationModuleBooter with org.modality_project.base.client.operationactionsloading.MongooseClientOperationActionsLoader;

}