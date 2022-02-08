// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.backoffice.activities.operations {

    // Direct dependencies modules
    requires javafx.graphics;
    requires mongoose.client.activity;
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
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.backoffice.activities.operations;
    exports mongoose.backoffice.activities.operations.routing;
    exports mongoose.backoffice.operations.routes.operations;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with mongoose.backoffice.activities.operations.RouteToOperationsRequestEmitter;
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.backoffice.activities.operations.OperationsUiRoute;

}