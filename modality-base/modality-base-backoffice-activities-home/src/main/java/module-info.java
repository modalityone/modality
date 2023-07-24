// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.activities.home {

  // Direct dependencies modules
  requires java.base;
  requires javafx.base;
  requires javafx.controls;
  requires javafx.graphics;
  requires modality.base.client.activity;
  requires modality.base.client.application;
  requires webfx.extras.scalepane;
  requires webfx.extras.theme;
  requires webfx.extras.util.layout;
  requires webfx.extras.webtext;
  requires webfx.kit.util;
  requires webfx.platform.windowhistory;
  requires webfx.stack.orm.domainmodel.activity;
  requires webfx.stack.routing.router;
  requires webfx.stack.routing.router.client;
  requires webfx.stack.routing.uirouter;
  requires webfx.stack.ui.action;
  requires webfx.stack.ui.operation;
  requires webfx.stack.ui.operation.action;

  // Exported packages
  exports one.modality.catering.backoffice.activities.home;
  exports one.modality.catering.backoffice.activities.home.routing;
  exports one.modality.catering.backoffice.operations.routes.home;

  // Provided services
  provides dev.webfx.stack.routing.uirouter.UiRoute with
      one.modality.catering.backoffice.activities.home.HomeUiRoute;
  provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with
      one.modality.catering.backoffice.activities.home.RouteToHomeRequestEmitter;
}
