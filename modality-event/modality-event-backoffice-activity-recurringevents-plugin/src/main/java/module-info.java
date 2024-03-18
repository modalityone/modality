// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.activity.recurringevents.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.event.backoffice.activities.recurringevents;
    exports one.modality.event.backoffice.activities.recurringevents.routing;
    exports one.modality.event.backoffice.operations.routes.recurringevents;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.backoffice.activities.recurringevents.BookingUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.backoffice.activities.recurringevents.RouteToRecurringEventsRequestEmitter;

}