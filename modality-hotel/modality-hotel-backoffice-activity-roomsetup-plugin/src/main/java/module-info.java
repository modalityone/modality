// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.hotel.backoffice.activity.roomsetup.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.client.activity.organizationdependent;
    requires modality.base.client.bootstrap;
    requires modality.base.client.cloud.image;
    requires modality.base.client.mainframe.fx;
    requires modality.base.client.tile;
    requires modality.base.shared.entities;
    requires modality.base.shared.knownitems;
    requires modality.crm.backoffice.organization.fx;
    requires webfx.extras.controlfactory;
    requires webfx.extras.filepicker;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.type;
    requires webfx.extras.util.control;
    requires webfx.extras.util.dialog;
    requires webfx.extras.validation;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.file;
    requires webfx.platform.scheduler;
    requires webfx.platform.uischeduler;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.binding;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.hotel.backoffice.activities.roomsetup;
    exports one.modality.hotel.backoffice.activities.roomsetup.dialog;
    exports one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.data;
    exports one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model;
    exports one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.presenter;
    exports one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.service;
    exports one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.view;
    exports one.modality.hotel.backoffice.activities.roomsetup.util;
    exports one.modality.hotel.backoffice.activities.roomsetup.view;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.hotel.backoffice.activities.roomsetup.RoomSetupRouting.RoomSetupUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.hotel.backoffice.activities.roomsetup.RoomSetupRouting.RouteToRoomSetupRequestEmitter;

}