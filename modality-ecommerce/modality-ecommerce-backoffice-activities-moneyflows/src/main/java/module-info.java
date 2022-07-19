// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.backoffice.activities.moneyflows {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.client.activity;
    requires modality.base.client.util;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.ecommerce.backoffice.operations.moneyflow;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.util;
    requires webfx.stack.async;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.platform.windowhistory;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;
    requires webfx.stack.ui.util;

    // Exported packages
    exports org.modality_project.ecommerce.backoffice.activities.moneyflows;
    exports org.modality_project.ecommerce.backoffice.activities.moneyflows.routing;
    exports org.modality_project.ecommerce.backoffice.operations.routes.moneyflows;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with org.modality_project.ecommerce.backoffice.activities.moneyflows.MoneyFlowsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with org.modality_project.ecommerce.backoffice.activities.moneyflows.RouteToMoneyFlowsRequestEmitter;

}