// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The login activity displayed by the UI router to an unauthenticated user trying to access a route requiring authorization.
 */
module modality.crm.client.activity.login.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.extras.i18n;
    requires webfx.extras.operation;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.authn.login.ui;
    requires webfx.stack.authn.login.ui.gateway.password.plugin;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.session.state.client.fx;

    // Exported packages
    exports one.modality.crm.client.activities.login;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.client.activities.login.LoginRouting.LoginUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.crm.client.activities.login.LoginRouting.RouteToLoginRequestEmitter;

}