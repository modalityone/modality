// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.activity.superadmin.home.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.backoffice.homemenu;
    requires modality.base.client.activity;
    requires webfx.extras.i18n;
    requires webfx.extras.operation;
    requires webfx.extras.operation.action;
    requires webfx.platform.conf;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.base.backoffice.activities.superadmin.home;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.base.backoffice.activities.superadmin.home.SuperAdminHomeRouting.SuperAdminHomeUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.base.backoffice.activities.superadmin.home.SuperAdminHomeRouting.RouteToSuperAdminHomeRequestEmitter;

}