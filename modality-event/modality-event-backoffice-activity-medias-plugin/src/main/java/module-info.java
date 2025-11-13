// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.activity.medias.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires modality.base.backoffice.activity.home.plugin;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.client.cloud.image;
    requires modality.base.client.i18n;
    requires modality.base.client.icons;
    requires modality.base.client.mainframe.fx;
    requires modality.base.client.messaging;
    requires modality.base.client.presentationmodel;
    requires modality.base.client.tile;
    requires modality.base.client.time;
    requires modality.base.client.util;
    requires modality.base.shared.entities;
    requires modality.base.shared.knownitems;
    requires modality.event.client.event.fx;
    requires webfx.extras.async;
    requires webfx.extras.cell;
    requires webfx.extras.controlfactory;
    requires webfx.extras.filepicker;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.extras.responsive;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.switches;
    requires webfx.extras.theme;
    requires webfx.extras.time.format;
    requires webfx.extras.util.control;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.masterslave;
    requires webfx.extras.validation;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.binding;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.platform.blob;

    // Exported packages
    exports one.modality.event.backoffice.activities.medias;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.backoffice.activities.medias.MediasRouting.MediasUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.backoffice.activities.medias.MediasRouting.RouteToMediasRequestEmitter;

}