// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.activities.login {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.validation;
    requires modality.event.client.sectionpanel;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.controls;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.authn;
    requires webfx.kit.util;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.crm.client.activities.login;

    // Provided services
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.crm.client.activities.login.LoginUiRoute;

}