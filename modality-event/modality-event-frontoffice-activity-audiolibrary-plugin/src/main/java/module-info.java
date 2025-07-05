// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activity.audiolibrary.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.bootstrap;
    requires modality.base.client.time;
    requires modality.base.client.util;
    requires modality.base.frontoffice.utility;
    requires modality.base.shared.entities;
    requires modality.crm.client.authn.fx;
    requires modality.crm.frontoffice.help;
    requires modality.crm.shared.authn;
    requires modality.event.frontoffice.eventheader;
    requires modality.event.frontoffice.medias;
    requires webfx.extras.cell;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.media.metadata;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.extras.player;
    requires webfx.extras.player.audio.javafxmedia;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.time.format;
    requires webfx.extras.util.control;
    requires webfx.extras.util.layout;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.blob;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.event.frontoffice.activities.audiolibrary;

    // Resources packages
    opens one.modality.event.frontoffice.activities.audiolibrary;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.audiolibrary.AudioLibraryRouting.AudioLibraryUiRoute, one.modality.event.frontoffice.activities.audiolibrary.EventAudioLibraryRouting.EventAudioLibraryUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.audiolibrary.AudioLibraryRouting.RouteToAudioLibraryRequestEmitter;

}