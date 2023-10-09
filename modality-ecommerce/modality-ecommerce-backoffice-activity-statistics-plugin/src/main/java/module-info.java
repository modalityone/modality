// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The statistics activity to view the number of booked options over the time.
 */
module modality.ecommerce.backoffice.activity.statistics.plugin {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.backoffice.tile;
    requires modality.base.client.activity.organizationdependent;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.util;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.client.activity.eventdependent;
    requires webfx.extras.canvas.bar;
    requires webfx.extras.canvas.pane;
    requires webfx.extras.geometry;
    requires webfx.extras.theme;
    requires webfx.extras.time.layout;
    requires webfx.extras.time.layout.gantt;
    requires webfx.extras.time.window;
    requires webfx.extras.util.control;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.ecommerce.backoffice.activities.statistics;
    exports one.modality.ecommerce.backoffice.activities.statistics.routing;
    exports one.modality.ecommerce.backoffice.operations.routes.statistics;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.ecommerce.backoffice.activities.statistics.StatisticsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.ecommerce.backoffice.activities.statistics.RouteToStatisticsRequestEmitter;

}