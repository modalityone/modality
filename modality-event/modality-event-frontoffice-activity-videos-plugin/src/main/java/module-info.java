// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activity.videos.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.web;
    requires modality.base.client.icons;
    requires modality.base.shared.entities;
    requires modality.crm.client.authn.fx;
    requires modality.event.frontoffice.medias;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.util.control;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.platform.util.time;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.event.frontoffice.activities.videos;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.videos.VideosRouting.VideosUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.videos.VideosRouting.RouteToVideosRequestEmitter;

}