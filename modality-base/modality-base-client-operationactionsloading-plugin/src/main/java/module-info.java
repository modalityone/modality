// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Module responsible for loading all information about operations from the database on client start.
 */
module modality.base.client.operationactionsloading.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires webfx.extras.util.control;
    requires webfx.platform.boot;
    requires webfx.platform.conf;
    requires webfx.platform.console;
    requires webfx.platform.meta;
    requires webfx.platform.scheduler;
    requires webfx.platform.util;
    requires webfx.stack.authz.client;
    requires webfx.stack.cache.client;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.entity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.exceptions;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.base.client.operationactionsloading;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationModuleBooter with one.modality.base.client.operationactionsloading.ModalityClientOperationActionsLoader;

}