// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.ecommerce.backoffice.activities.moneyflows {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.base.backoffice.masterslave;
    requires mongoose.base.client.activity;
    requires mongoose.base.client.util;
    requires mongoose.base.shared.domainmodel;
    requires mongoose.base.shared.entities;
    requires mongoose.ecommerce.backoffice.operations.moneyflow;
    requires webfx.extras.visual.base;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.controls;
    requires webfx.framework.client.operationaction;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.entity.controls;
    requires webfx.framework.client.orm.reactive.dql;
    requires webfx.framework.client.orm.reactive.entities;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.router;
    requires webfx.kit.util;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.submit;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.ecommerce.backoffice.activities.moneyflows;
    exports mongoose.ecommerce.backoffice.activities.moneyflows.routing;
    exports mongoose.ecommerce.backoffice.operations.routes.moneyflows;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with mongoose.ecommerce.backoffice.activities.moneyflows.RouteToMoneyFlowsRequestEmitter;
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.ecommerce.backoffice.activities.moneyflows.MoneyFlowsUiRoute;

}