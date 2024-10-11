// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activity.audiorecordings.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.icons;
    requires modality.base.shared.entities;
    requires modality.crm.client.authn.fx;
    requires modality.event.client.mediaview;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.util.control;
    requires webfx.platform.blob;
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
    exports one.modality.event.frontoffice.activities.audiorecordings;
    exports one.modality.event.frontoffice.activities.audiorecordings.routing;
    exports one.modality.event.frontoffice.operations.routes.audiorecordings;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.audiorecordings.AudioRecordingsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.audiorecordings.RouteToAudioRecordingsRequestEmitter;

}