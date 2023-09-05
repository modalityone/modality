// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The unauthorised activity displayed by the UI router to an authenticated user trying to access an unauthorised route.
 */
module modality.crm.client.activity.unauthorized.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.crm.client.activities.unauthorized;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.client.activities.unauthorized.UnauthorizedUiRoute;

}