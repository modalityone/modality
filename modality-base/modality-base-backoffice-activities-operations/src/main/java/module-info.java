// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.activities.operations {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.entity.controls;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.router;
    requires webfx.kit.launcher;
    requires webfx.platform.shared.util;
    requires webfx.stack.async;
    requires webfx.stack.platform.windowhistory;

    // Exported packages
    exports org.modality_project.base.backoffice.activities.operations;
    exports org.modality_project.base.backoffice.activities.operations.routing;
    exports org.modality_project.base.backoffice.operations.routes.operations;

    // Provided services
    provides dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter with org.modality_project.base.backoffice.activities.operations.RouteToOperationsRequestEmitter;
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.base.backoffice.activities.operations.OperationsUiRoute;

}