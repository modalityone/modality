// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activities.alerts {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.frontoffice.utility;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.event.frontoffice.activities.alerts;
    exports one.modality.event.frontoffice.activities.alerts.routing;
    exports one.modality.event.frontoffice.operations.routes.alerts;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.alerts.AlertsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.alerts.RouteToAlertsRequestEmitter;

}