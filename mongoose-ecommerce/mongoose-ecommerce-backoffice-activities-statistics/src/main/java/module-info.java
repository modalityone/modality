// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.ecommerce.backoffice.activities.statistics {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.base.backoffice.masterslave;
    requires mongoose.base.backoffice.operations.generic;
    requires mongoose.base.client.activity;
    requires mongoose.base.client.presentationmodel;
    requires mongoose.base.client.util;
    requires mongoose.base.shared.entities;
    requires mongoose.crm.backoffice.bookingdetailspanel;
    requires mongoose.ecommerce.backoffice.operations.document;
    requires mongoose.ecommerce.backoffice.operations.documentline;
    requires webfx.extras.type;
    requires webfx.extras.visual.base;
    requires webfx.extras.visual.controls.grid;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.operationaction;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.dql;
    requires webfx.framework.client.orm.reactive.entities;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.orm.expression;
    requires webfx.framework.shared.router;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.ecommerce.backoffice.activities.statistics;
    exports mongoose.ecommerce.backoffice.activities.statistics.routing;
    exports mongoose.ecommerce.backoffice.operations.routes.statistics;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with mongoose.ecommerce.backoffice.activities.statistics.RouteToStatisticsRequestEmitter;
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.ecommerce.backoffice.activities.statistics.StatisticsUiRoute;

}