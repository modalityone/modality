// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.backoffice.activities.authorizations {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.crm.client.authn;
    requires webfx.extras.visual.controls.grid;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.entity.controls;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.router;
    requires webfx.platform.util;
    requires webfx.stack.async;
    requires webfx.stack.platform.windowhistory;

    // Exported packages
    exports org.modality_project.crm.backoffice.activities.authorizations;
    exports org.modality_project.crm.backoffice.activities.authorizations.routing;
    exports org.modality_project.crm.backoffice.activities.operations.authorizations;

    // Provided services
    provides dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter with org.modality_project.crm.backoffice.activities.authorizations.RouteToAuthorizationsRequestEmitter;
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.crm.backoffice.activities.authorizations.AuthorizationsUiRoute;

}