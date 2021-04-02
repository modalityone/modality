// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.client.activities.unauthorized {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.client.activities.unauthorized;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.client.activities.unauthorized.UnauthorizedUiRoute;

}