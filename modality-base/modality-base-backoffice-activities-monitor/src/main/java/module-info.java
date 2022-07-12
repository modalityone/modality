// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.activities.monitor {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires webfx.extras.visual;
    requires webfx.extras.visual.charts;
    requires webfx.platform.util;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.platform.windowhistory;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.util;

    // Exported packages
    exports org.modality_project.base.backoffice.activities.monitor;
    exports org.modality_project.base.backoffice.activities.monitor.routing;
    exports org.modality_project.base.backoffice.operations.routes.monitor;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with org.modality_project.base.backoffice.activities.monitor.MonitorUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with org.modality_project.base.backoffice.activities.monitor.RouteToMonitorRequestEmitter;

}