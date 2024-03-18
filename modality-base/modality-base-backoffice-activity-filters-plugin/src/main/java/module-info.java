// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The back-office Filters activity to create, modify, delete filters (displayed in other activities).
 */
module modality.base.backoffice.activity.filters.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.backoffice.operations.filters;
    requires modality.base.client.activity.organizationdependent;
    requires modality.base.shared.entities;
    requires webfx.extras.cell;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.base.backoffice.activities.filters;
    exports one.modality.base.backoffice.activities.filters.routing;
    exports one.modality.base.backoffice.operations.routes.filters;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.base.backoffice.activities.filters.FiltersUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.base.backoffice.activities.filters.RouteToFiltersRequestEmitter;

}