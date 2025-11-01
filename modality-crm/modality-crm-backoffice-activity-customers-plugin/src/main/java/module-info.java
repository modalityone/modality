// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.backoffice.activity.customers.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.bootstrap;
    requires modality.base.client.mainframe.fx;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n;
    requires webfx.extras.operation;
    requires webfx.extras.util.dialog;
    requires webfx.extras.util.layout;
    requires webfx.kit.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.crm.backoffice.activities.customers;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.backoffice.activities.customers.CustomersRouting.CustomersUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.crm.backoffice.activities.customers.CustomersRouting.RouteToCustomersRequestEmitter;

}