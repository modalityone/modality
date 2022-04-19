// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.event.backoffice.activities.events {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires mongoose.base.client.activity;
    requires mongoose.base.client.util;
    requires mongoose.ecommerce.backoffice.activities.bookings;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.router;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.event.backoffice.activities.events;
    exports mongoose.event.backoffice.activities.events.routing;
    exports mongoose.event.backoffice.operations.routes.events;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with mongoose.event.backoffice.activities.events.RouteToEventsRequestEmitter;
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.event.backoffice.activities.events.EventsUiRoute;

}