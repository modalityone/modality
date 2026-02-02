// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The Kitchen activity that displays the meals figures for the selected month.
 */
module modality.catering.backoffice.activity.kitchen.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.client.activity;
    requires modality.base.client.bootstrap;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.icons;
    requires modality.base.shared.entities;
    requires modality.base.shared.knownitems;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.client.activity.eventdependent;
    requires webfx.extras.canvas.pane;
    requires webfx.extras.geometry;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.operation.action;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.theme;
    requires webfx.extras.time.layout;
    requires webfx.extras.time.layout.gantt;
    requires webfx.extras.time.pickers;
    requires webfx.extras.util.control;
    requires webfx.extras.util.dialog;
    requires webfx.kit.util;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.catering.backoffice.activities.kitchen;
    exports one.modality.catering.backoffice.activities.kitchen.controller;
    exports one.modality.catering.backoffice.activities.kitchen.model;
    exports one.modality.catering.backoffice.activities.kitchen.service;
    exports one.modality.catering.backoffice.activities.kitchen.view;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.catering.backoffice.activities.kitchen.KitchenRouting.KitchenUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.catering.backoffice.activities.kitchen.KitchenRouting.RouteToKitchenRequestEmitter;

}