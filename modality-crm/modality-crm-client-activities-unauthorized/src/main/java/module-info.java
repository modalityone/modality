// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.activities.unauthorized {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.crm.client.activities.unauthorized;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with org.modality_project.crm.client.activities.unauthorized.UnauthorizedUiRoute;

}