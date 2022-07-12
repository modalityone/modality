// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.backoffice.activities.authorizations {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.crm.client.authn;
    requires webfx.extras.visual.grid;
    requires webfx.platform.util;
    requires webfx.stack.async;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.platform.windowhistory;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports org.modality_project.crm.backoffice.activities.authorizations;
    exports org.modality_project.crm.backoffice.activities.authorizations.routing;
    exports org.modality_project.crm.backoffice.activities.operations.authorizations;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with org.modality_project.crm.backoffice.activities.authorizations.AuthorizationsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with org.modality_project.crm.backoffice.activities.authorizations.RouteToAuthorizationsRequestEmitter;

}