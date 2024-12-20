// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activity.library.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.application;
    requires modality.event.frontoffice.activity.audiorecordings.plugin;
    requires modality.event.frontoffice.activity.videos.plugin;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.event.frontoffice.activities.library;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.library.LibraryRouting.LibraryUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.library.LibraryRouting.RouteToLibraryRequestEmitter;

}