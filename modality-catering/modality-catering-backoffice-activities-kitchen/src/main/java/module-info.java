// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.catering.backoffice.activities.kitchen {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires webfx.extras.scalepane;
    requires webfx.kit.util;
    requires webfx.platform.json;
    requires webfx.platform.windowhistory;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.fxraiser;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.util;

    // Exported packages
    exports one.modality.catering.backoffice.activities.kitchen;
    exports one.modality.catering.backoffice.activities.kitchen.routing;
    exports one.modality.catering.backoffice.operations.routes.kitchen;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.catering.backoffice.activities.kitchen.KitchenUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.catering.backoffice.activities.kitchen.RouteToKitchenRequestEmitter;

}