// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.activities.filters {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.backoffice.operations.filters;
    requires modality.base.client.activity;
    requires modality.base.shared.entities;
    requires webfx.extras.cell;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.util;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.platform.windowhistory;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports org.modality_project.base.backoffice.activities.filters;
    exports org.modality_project.base.backoffice.activities.filters.routing;
    exports org.modality_project.base.backoffice.operations.routes.filters;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with org.modality_project.base.backoffice.activities.filters.FiltersUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with org.modality_project.base.backoffice.activities.filters.RouteToFiltersRequestEmitter;

}