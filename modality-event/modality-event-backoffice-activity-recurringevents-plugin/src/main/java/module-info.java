// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.activity.recurringevents.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.headertabs.fx;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.icons;
    requires modality.base.client.mainframe.dialogarea.fx;
    requires modality.base.client.tile;
    requires modality.base.client.validation;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.client.event.fx;
    requires webfx.extras.cell;
    requires webfx.extras.filepicker;
    requires webfx.extras.panes;
    requires webfx.extras.switches;
    requires webfx.extras.theme;
    requires webfx.extras.time;
    requires webfx.extras.time.layout;
    requires webfx.extras.time.layout.calendar;
    requires webfx.extras.util.control;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.masterslave;
    requires webfx.extras.visual.grid;
    requires webfx.extras.webtext;
    requires webfx.kit.util;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.file;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.cloud.image;
    requires webfx.stack.cloud.image.client;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.dialog;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.event.backoffice.activities.recurringevents;
    exports one.modality.event.backoffice.activities.recurringevents.routing;
    exports one.modality.event.backoffice.operations.routes.recurringevents;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.backoffice.activities.recurringevents.BookingUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.backoffice.activities.recurringevents.RouteToRecurringEventsRequestEmitter;

}