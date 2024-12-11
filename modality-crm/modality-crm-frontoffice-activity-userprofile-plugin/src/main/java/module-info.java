// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.frontoffice.activity.userprofile.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.crm.frontoffice.activities.userprofile;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.frontoffice.activities.userprofile.UserProfileRouting.UserProfileUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.crm.frontoffice.activities.userprofile.UserProfileRouting.RouteToUserProfileRequestEmitter;

}