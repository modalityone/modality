// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.backoffice.activities.organizations {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.event.backoffice.activities.events;
    requires webfx.platform.async;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports org.modality_project.crm.backoffice.activities.organizations;
    exports org.modality_project.crm.backoffice.activities.organizations.routing;
    exports org.modality_project.crm.backoffice.operations.routes.organizations;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with org.modality_project.crm.backoffice.activities.organizations.OrganizationsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with org.modality_project.crm.backoffice.activities.organizations.RouteToOrganizationsRequestEmitter;

}