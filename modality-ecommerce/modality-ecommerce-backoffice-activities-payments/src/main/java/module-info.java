// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.backoffice.activities.payments {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.backoffice.operations.generic;
    requires modality.base.client.activity;
    requires modality.base.client.presentationmodel;
    requires modality.base.client.util;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.ecommerce.backoffice.operations.moneytransfer;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.platform.util;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.expression;
    requires webfx.stack.orm.reactive.dql;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.platform.windowhistory;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;
    requires webfx.stack.ui.util;

    // Exported packages
    exports org.modality_project.ecommerce.backoffice.activities.payments;
    exports org.modality_project.ecommerce.backoffice.activities.payments.routing;
    exports org.modality_project.ecommerce.backoffice.operations.routes.payments;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with org.modality_project.ecommerce.backoffice.activities.payments.PaymentsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with org.modality_project.ecommerce.backoffice.activities.payments.RouteToPaymentsRequestEmitter;

}