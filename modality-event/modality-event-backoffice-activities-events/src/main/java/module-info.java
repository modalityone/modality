// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.activities.events {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.util;
    requires modality.ecommerce.backoffice.activities.bookings;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.event.backoffice.activities.events;
    exports one.modality.event.backoffice.activities.events.routing;
    exports one.modality.event.backoffice.operations.routes.events;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.backoffice.activities.events.EventsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.backoffice.activities.events.RouteToEventsRequestEmitter;

}