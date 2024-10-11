// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.activity.medias.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.client.icons;
    requires modality.base.client.tile;
    requires modality.base.client.util;
    requires modality.base.client.validation;
    requires modality.base.shared.entities;
    requires modality.event.client.event.fx;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.switches;
    requires webfx.extras.theme;
    requires webfx.extras.util.control;
    requires webfx.extras.util.masterslave;
    requires webfx.extras.webtext;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.event.backoffice.activities.medias;
    exports one.modality.event.backoffice.activities.medias.routing;
    exports one.modality.event.backoffice.operations.routes.medias;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.backoffice.activities.medias.MediasUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.backoffice.activities.medias.RouteToMediasRequestEmitter;

}