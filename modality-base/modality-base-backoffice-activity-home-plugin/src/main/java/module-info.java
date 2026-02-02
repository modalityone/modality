// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The back-office Home page that displays activities as browsable tiles (only the authorized ones are displayed).
 */
module modality.base.backoffice.activity.home.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.homemenu;
    requires modality.base.client.activity;
    requires modality.crm.backoffice.organization.fx;
    requires webfx.extras.i18n;
    requires webfx.extras.operation;
    requires webfx.extras.operation.action;
    requires webfx.kit.util;
    requires webfx.platform.conf;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.base.backoffice.activities.home;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.base.backoffice.activities.home.BackOfficeHomeRouting.HomeUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.base.backoffice.activities.home.BackOfficeHomeRouting.RouteToHomeRequestEmitter;

}