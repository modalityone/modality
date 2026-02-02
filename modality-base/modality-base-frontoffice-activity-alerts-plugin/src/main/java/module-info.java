// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The front-office Alerts activity.
 */
module modality.base.frontoffice.activity.alerts.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.extras.operation;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.base.frontoffice.activities.alerts;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.base.frontoffice.activities.alerts.AlertsRouting.AlertsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.base.frontoffice.activities.alerts.AlertsRouting.RouteToAlertsRequestEmitter;

}