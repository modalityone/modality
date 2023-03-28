// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.activities.login {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.validation;
    requires modality.event.client.sectionpanel;
    requires webfx.extras.util.animation;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.scene;
    requires webfx.kit.util;
    requires webfx.stack.authn;
    requires webfx.stack.authn.login.ui;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.session.state.client.fx;
    requires webfx.stack.ui.controls;

    // Exported packages
    exports one.modality.crm.client.activities.login;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.client.activities.login.LoginUiRoute;

}