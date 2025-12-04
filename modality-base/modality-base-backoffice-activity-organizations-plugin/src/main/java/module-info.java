// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.activity.organizations.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.extras.i18n;
    requires webfx.extras.operation;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.base.backoffice.activities.organizations;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.base.backoffice.activities.organizations.OrganizationsRouting.OrganizationsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.base.backoffice.activities.organizations.OrganizationsRouting.RouteToOrganizationsRequestEmitter;

}