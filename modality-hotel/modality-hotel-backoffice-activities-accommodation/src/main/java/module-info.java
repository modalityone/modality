// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.hotel.backoffice.activities.accommodation {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;
    requires modality.base.client.presentationmodel;
    requires modality.base.backoffice.masterslave;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.extras.timelayout;
    requires modality.base.client.gantt.fx;

    // Exported packages
    exports one.modality.hotel.backoffice.activities.accommodation;
    exports one.modality.hotel.backoffice.activities.accommodation.routing;
    exports one.modality.hotel.backoffice.operations.routes.accommodation;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.hotel.backoffice.activities.accommodation.AccommodationUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.hotel.backoffice.activities.accommodation.RouteToAccommodationRequestEmitter;

}