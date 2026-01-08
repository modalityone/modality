// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.booking.backoffice.activity.registration.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.backoffice.masterslave;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.mainframe.fx;
    requires modality.base.client.presentationmodel;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.client.activity.eventdependent;
    requires modality.event.client.event.fx;
    requires webfx.extras.canvas.bar;
    requires webfx.extras.cell;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.operation.action;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.time;
    requires webfx.extras.time.layout;
    requires webfx.extras.time.layout.gantt;
    requires webfx.extras.time.pickers;
    requires webfx.extras.util.dialog;
    requires webfx.extras.validation;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.binding;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.expression;
    requires webfx.stack.orm.reactive.dql;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.booking.backoffice.activities.registration;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.booking.backoffice.activities.registration.RegistrationRouting.RegistrationUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.booking.backoffice.activities.registration.RegistrationRouting.RouteToRegistrationRequestEmitter;

}