// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.activity.createevent.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.i18n;
    requires modality.base.client.mainframe.fx;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.backoffice.eventcreator;
    requires webfx.extras.async;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.operation.action;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.styles.materialdesign;
    requires webfx.extras.time.pickers;
    requires webfx.extras.validation;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.event.backoffice.activities.createevent;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.backoffice.activities.createevent.CreateEventRouting.CreateEventUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.backoffice.activities.createevent.CreateEventRouting.RouteToCreateEventRequestEmitter;

}