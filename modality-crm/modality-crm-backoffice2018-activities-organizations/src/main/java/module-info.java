// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.backoffice2018.activities.organizations {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.crm.backoffice2018.activities.organizations;
    exports one.modality.crm.backoffice2018.activities.organizations.routing;
    exports one.modality.crm.backoffice2018.operations.routes.organizations;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.backoffice2018.activities.organizations.OrganizationsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.crm.backoffice2018.activities.organizations.RouteToOrganizationsRequestEmitter;

}