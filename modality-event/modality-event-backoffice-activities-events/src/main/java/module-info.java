// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.activities.events {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.util;
    requires modality.ecommerce.backoffice.activities.bookings;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.router;
    requires webfx.platform.shared.util;
    requires webfx.stack.async;
    requires webfx.stack.platform.windowhistory;

    // Exported packages
    exports org.modality_project.event.backoffice.activities.events;
    exports org.modality_project.event.backoffice.activities.events.routing;
    exports org.modality_project.event.backoffice.operations.routes.events;

    // Provided services
    provides dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter with org.modality_project.event.backoffice.activities.events.RouteToEventsRequestEmitter;
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.event.backoffice.activities.events.EventsUiRoute;

}