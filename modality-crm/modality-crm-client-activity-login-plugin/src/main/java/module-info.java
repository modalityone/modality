// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The login activity displayed by the UI router to an unauthenticated user trying to access a route requiring authorization.
 */
module modality.crm.client.activity.login.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.stack.authn.login.ui;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.session.state.client.fx;

    // Exported packages
    exports one.modality.crm.client.activities.login;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.client.activities.login.LoginUiRoute;

}