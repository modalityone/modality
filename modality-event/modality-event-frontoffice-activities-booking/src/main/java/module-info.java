// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activities.booking {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.frontoffice.utility;
    requires modality.base.shared.entities;
    requires modality.crm.shared.authn;
    requires webfx.extras.util.layout;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.session.state.client.fx;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.event.frontoffice.activities.booking;
    exports one.modality.event.frontoffice.activities.booking.routing;
    exports one.modality.event.frontoffice.operations.routes.booking;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.booking.BookingUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.booking.RouteToBookingRequestEmitter;

}