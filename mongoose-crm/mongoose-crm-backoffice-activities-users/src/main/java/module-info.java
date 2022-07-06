// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.crm.backoffice.activities.users {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires mongoose.base.backoffice.masterslave;
    requires mongoose.base.client.activity;
    requires mongoose.base.shared.domainmodel;
    requires mongoose.base.shared.entities;
    requires webfx.extras.visual.base;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.dql;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.expression;
    requires webfx.framework.shared.router;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.util;
    requires webfx.framework.client.action;
    requires mongoose.ecommerce.backoffice.operations.document;
    requires mongoose.base.client.aggregates;

    // Exported packages
    exports mongoose.crm.backoffice.activities.users;
    exports mongoose.crm.backoffice.activities.users.routing;
    exports mongoose.crm.backoffice.operations.routes.users;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with mongoose.crm.backoffice.activities.users.RouteToUsersRequestEmitter;
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.crm.backoffice.activities.users.UsersUiRoute;

}