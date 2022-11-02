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
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.util;

    // Exported packages
    exports one.modality.base.backoffice.activities.monitor;
    exports one.modality.base.backoffice.activities.monitor.routing;
    exports one.modality.base.backoffice.operations.routes.monitor;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.base.backoffice.activities.monitor.MonitorUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.base.backoffice.activities.monitor.RouteToMonitorRequestEmitter;

}