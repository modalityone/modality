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
    requires modality.base.shared.entities;
    requires modality.hotel.backoffice.accommodation;
    requires modality.hotel.backoffice.operations.documentline;
    requires webfx.extras.geometry;
    requires webfx.extras.time.layout;
    requires webfx.extras.time.layout.gantt;
    requires webfx.extras.time.window;
    requires webfx.kit.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.cache.client;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.hotel.backoffice.activities.household;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.hotel.backoffice.activities.household.HouseholdRouting.HouseholdUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.hotel.backoffice.activities.household.HouseholdRouting.RouteToHouseholdRequestEmitter;

}