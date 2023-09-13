// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The back-office Home page that displays activities as browsable tiles (only the authorized ones are displayed).
 */
module modality.base.backoffice.activity.home.plugin {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires modality.base.backoffice.tile;
    requires modality.base.client.activity;
    requires modality.base.client.application;
    requires webfx.extras.theme;
    requires webfx.platform.conf;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.base.backoffice.activities.home;
    exports one.modality.base.backoffice.activities.home.routing;
    exports one.modality.base.backoffice.operations.routes.home;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.base.backoffice.activities.home.HomeUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.base.backoffice.activities.home.RouteToHomeRequestEmitter;

}