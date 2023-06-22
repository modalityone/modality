// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activities.booking {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.frontoffice.utility;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.event.frontoffice.activities.booking;
    exports one.modality.event.frontoffice.activities.booking.routing;
    exports one.modality.event.frontoffice.operations.routes.booking;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.booking.BookingUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.booking.RouteToBookingRequestEmitter;

}