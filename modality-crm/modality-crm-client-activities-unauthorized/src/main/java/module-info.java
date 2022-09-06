// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.activities.unauthorized {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.platform.util;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.crm.client.activities.unauthorized;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.client.activities.unauthorized.UnauthorizedUiRoute;

}