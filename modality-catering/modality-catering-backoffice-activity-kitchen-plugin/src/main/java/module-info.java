// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The Kitchen activity that displays the meals figures for the selected month.
 */
module modality.catering.backoffice.activity.kitchen.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.time.theme;
    requires modality.base.shared.entities;
    requires modality.catering.client.i18n;
    requires modality.crm.backoffice.organization.fx;
    requires webfx.extras.panes;
    requires webfx.extras.theme;
    requires webfx.extras.time;
    requires webfx.extras.time.layout;
    requires webfx.extras.time.layout.calendar;
    requires webfx.extras.util.control;
    requires webfx.extras.util.layout;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.platform.uischeduler;
    requires webfx.platform.windowhistory;
    requires webfx.stack.cache.client;
    requires webfx.stack.db.query;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.call;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.fxraiser;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.catering.backoffice.activities.kitchen;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.catering.backoffice.activities.kitchen.KitchenRouting.KitchenUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.catering.backoffice.activities.kitchen.KitchenRouting.RouteToKitchenRequestEmitter;

}