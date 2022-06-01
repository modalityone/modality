// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.base.backoffice.activities.filters {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.base.backoffice.masterslave;
    requires mongoose.base.backoffice.operations.generic;
    requires mongoose.base.client.activity;
    requires mongoose.base.client.aggregates;
    requires mongoose.base.client.presentationmodel;
    requires mongoose.base.client.util;
    requires mongoose.base.shared.domainmodel;
    requires mongoose.base.shared.entities;
    requires mongoose.crm.backoffice.bookingdetailspanel;
    requires mongoose.ecommerce.backoffice.operations.document;
    requires mongoose.event.backoffice.activities.cloneevent.routing;
    requires mongoose.event.frontoffice.activities.fees;
    requires webfx.extras.visual.base;
    requires webfx.extras.visual.controls.grid;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.operationaction;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.dql;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.expression;
    requires webfx.framework.shared.router;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.util;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.framework.shared.orm.entity;

    // Exported packages
    exports mongoose.base.backoffice.activities.filters;
    exports mongoose.base.backoffice.activities.filters.routing;
    exports mongoose.base.backoffice.operations.routes.filters;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with mongoose.base.backoffice.activities.filters.RouteToFiltersRequestEmitter;
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.base.backoffice.activities.filters.FiltersUiRoute;

}