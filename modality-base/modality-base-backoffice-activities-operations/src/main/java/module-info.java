// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.activities.operations {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires webfx.kit.launcher;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.base.backoffice.activities.operations;
    exports one.modality.base.backoffice.activities.operations.routing;
    exports one.modality.base.backoffice.operations.routes.operations;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.base.backoffice.activities.operations.OperationsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.base.backoffice.activities.operations.RouteToOperationsRequestEmitter;

}