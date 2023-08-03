// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.activities.login {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.stack.authn.login.ui;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.crm.client.activities.login;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.client.activities.login.LoginUiRoute;

}