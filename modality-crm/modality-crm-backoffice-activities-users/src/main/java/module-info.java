// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.backoffice.activities.users {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.client.activity;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.ecommerce.backoffice.operations.document;
    requires webfx.extras.visual;
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

    // Exported packages
    exports org.modality_project.crm.backoffice.activities.users;
    exports org.modality_project.crm.backoffice.activities.users.routing;
    exports org.modality_project.crm.backoffice.operations.routes.users;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with org.modality_project.crm.backoffice.activities.users.UsersUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with org.modality_project.crm.backoffice.activities.users.RouteToUsersRequestEmitter;

}