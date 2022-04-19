// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.catering.backoffice.activities.diningareas {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.base.backoffice.masterslave;
    requires mongoose.base.backoffice.operations.generic;
    requires mongoose.base.client.activity;
    requires mongoose.base.client.util;
    requires mongoose.base.shared.entities;
    requires mongoose.catering.backoffice.operations.allocationrule;
    requires mongoose.ecommerce.backoffice.activities.statistics;
    requires webfx.extras.visual.base;
    requires webfx.extras.visual.controls.grid;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.operationaction;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.router;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.catering.backoffice.activities.diningareas;
    exports mongoose.catering.backoffice.activities.diningareas.routing;
    exports mongoose.catering.backoffice.operations.routes.diningareas;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with mongoose.catering.backoffice.activities.diningareas.RouteToDiningAreasRequestEmitter;
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.catering.backoffice.activities.diningareas.DiningAreasUiRoute;

}