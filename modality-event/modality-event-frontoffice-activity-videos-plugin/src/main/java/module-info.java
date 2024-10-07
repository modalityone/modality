// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activity.videos.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.event.frontoffice.activities.videos;
    exports one.modality.event.frontoffice.activities.videos.routing;
    exports one.modality.event.frontoffice.operations.routes.videos;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.videos.VideosUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.videos.RouteToVideosRequestEmitter;

}