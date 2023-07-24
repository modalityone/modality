// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.hotel.backoffice.activities.accommodation {

  // Direct dependencies modules
  requires java.base;
  requires javafx.base;
  requires javafx.controls;
  requires javafx.graphics;
  requires modality.base.client.activity;
  requires modality.base.client.gantt.fx;
  requires modality.base.shared.entities;
  requires modality.hotel.backoffice.accommodation;
  requires webfx.extras.geometry;
  requires webfx.extras.time.layout;
  requires webfx.extras.util.layout;
  requires webfx.kit.util;
  requires webfx.platform.windowhistory;
  requires webfx.stack.orm.domainmodel.activity;
  requires webfx.stack.routing.router;
  requires webfx.stack.routing.router.client;
  requires webfx.stack.routing.uirouter;
  requires webfx.stack.ui.operation;
  requires webfx.stack.ui.operation.action;

  // Exported packages
  exports one.modality.hotel.backoffice.activities.accommodation;
  exports one.modality.hotel.backoffice.activities.accommodation.routing;
  exports one.modality.hotel.backoffice.operations.routes.accommodation;

  // Provided services
  provides dev.webfx.stack.routing.uirouter.UiRoute with
      one.modality.hotel.backoffice.activities.accommodation.AccommodationUiRoute;
  provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with
      one.modality.hotel.backoffice.activities.accommodation.RouteToAccommodationRequestEmitter;
}
