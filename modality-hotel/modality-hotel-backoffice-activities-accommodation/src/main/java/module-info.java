// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.hotel.backoffice.activities.accommodation {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.client.activity;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.presentationmodel;
    requires modality.base.shared.entities;
    requires webfx.extras.time;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;
    requires modality.hotel.backoffice.activities.roomcalendar;
    requires webfx.extras.geometry;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.extras.canvas.pane;
    requires webfx.extras.time.layout;
    requires webfx.extras.time.layout.gantt;
    requires webfx.extras.theme;
    requires modality.crm.backoffice.organization.fx;
    requires webfx.extras.canvas.bar;
    requires webfx.extras.util.layout;
    requires webfx.extras.time.window;

    // Exported packages
    exports one.modality.hotel.backoffice.activities.accommodation;
    exports one.modality.hotel.backoffice.activities.accommodation.routing;
    exports one.modality.hotel.backoffice.operations.routes.accommodation;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.hotel.backoffice.activities.accommodation.AccommodationUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.hotel.backoffice.activities.accommodation.RouteToAccommodationRequestEmitter;

}