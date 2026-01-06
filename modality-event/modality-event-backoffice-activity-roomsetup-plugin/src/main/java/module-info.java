// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.activity.roomsetup.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.client.activity;
    requires modality.base.client.bootstrap;
    requires modality.base.client.mainframe.fx;
    requires modality.base.client.tile;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.client.event.fx;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.operation.action;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.theme;
    requires webfx.extras.util.control;
    requires webfx.extras.util.dialog;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.event.backoffice.activities.roomsetup;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.backoffice.activities.roomsetup.EventRoomSetupRouting.EventRoomSetupUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.backoffice.activities.roomsetup.EventRoomSetupRouting.RouteToEventRoomSetupRequestEmitter;

}