// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The money flows activity to define the Organization money accounts and the possible money flows between them.
 */
module modality.ecommerce.backoffice.activity.moneyflows.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.client.activity.organizationdependent;
    requires modality.base.client.util;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.ecommerce.backoffice.operations.moneyflow;
    requires modality.event.client.activity.eventdependent;
    requires webfx.extras.action;
    requires webfx.extras.controlfactory;
    requires webfx.extras.operation;
    requires webfx.extras.operation.action;
    requires webfx.extras.util.background;
    requires webfx.extras.util.border;
    requires webfx.extras.util.dialog;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.ecommerce.backoffice.activities.moneyflows;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.ecommerce.backoffice.activities.moneyflows.MoneyFlowsRouting.MoneyFlowsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.ecommerce.backoffice.activities.moneyflows.MoneyFlowsRouting.RouteToMoneyFlowsRequestEmitter;

}