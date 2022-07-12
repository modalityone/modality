// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.activities.monitor {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires webfx.extras.visual;
    requires webfx.extras.visual.charts;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.entities;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.router;
    requires webfx.platform.util;
    requires webfx.stack.platform.windowhistory;

    // Exported packages
    exports org.modality_project.base.backoffice.activities.monitor;
    exports org.modality_project.base.backoffice.activities.monitor.routing;
    exports org.modality_project.base.backoffice.operations.routes.monitor;

    // Provided services
    provides dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter with org.modality_project.base.backoffice.activities.monitor.RouteToMonitorRequestEmitter;
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.base.backoffice.activities.monitor.MonitorUiRoute;

}