// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The back-office Household activity.
 */
module modality.hotel.backoffice.activities.household {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.activity.home.plugin;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.client.activity.organizationdependent;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.tile;
    requires modality.base.shared.entities;
    requires modality.event.backoffice.events.ganttcanvas.plugin;
    requires modality.hotel.backoffice.accommodation;
    requires webfx.extras.canvas.bar;
    requires webfx.extras.geometry;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.operation.action;
    requires webfx.extras.time.layout;
    requires webfx.extras.time.layout.gantt;
    requires webfx.extras.time.window;
    requires webfx.extras.util.control;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.hotel.backoffice.activities.household;
    exports one.modality.hotel.backoffice.activities.household.dashboard.data;
    exports one.modality.hotel.backoffice.activities.household.dashboard.model;
    exports one.modality.hotel.backoffice.activities.household.dashboard.presenter;
    exports one.modality.hotel.backoffice.activities.household.dashboard.view;
    exports one.modality.hotel.backoffice.activities.household.gantt.adapter;
    exports one.modality.hotel.backoffice.activities.household.gantt.canvas;
    exports one.modality.hotel.backoffice.activities.household.gantt.data;
    exports one.modality.hotel.backoffice.activities.household.gantt.model;
    exports one.modality.hotel.backoffice.activities.household.gantt.presenter;
    exports one.modality.hotel.backoffice.activities.household.gantt.renderer;
    exports one.modality.hotel.backoffice.activities.household.gantt.view;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.hotel.backoffice.activities.household.HouseholdRouting.HouseholdUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.hotel.backoffice.activities.household.HouseholdRouting.RouteToHouseholdRequestEmitter;

}