// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activity.videostreaming.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.i18n;
    requires modality.base.client.icons;
    requires modality.base.client.messaging;
    requires modality.base.client.time;
    requires modality.base.frontoffice.utility;
    requires modality.base.shared.entities;
    requires modality.crm.client.authn.fx;
    requires modality.crm.frontoffice.help;
    requires modality.crm.shared.authn;
    requires modality.event.client.i18n;
    requires modality.event.frontoffice.eventheader;
    requires modality.event.frontoffice.medias;
    requires webfx.extras.aria;
    requires webfx.extras.cell;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.extras.player;
    requires webfx.extras.player.multi.all;
    requires webfx.extras.responsive;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.time.format;
    requires webfx.extras.type;
    requires webfx.extras.util.control;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.scene;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.extras.webtext;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.scheduler;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.platform.util.time;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.binding;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.event.frontoffice.activities.videostreaming;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.videostreaming.VideoStreamingRouting.VideoStreamingUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.videostreaming.VideoStreamingRouting.RouteToVideoStreamingRequestEmitter;

}