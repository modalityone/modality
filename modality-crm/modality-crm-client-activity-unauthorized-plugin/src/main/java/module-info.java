// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The unauthorised activity displayed by the UI router to an authenticated user trying to access an unauthorised route.
 */
module modality.crm.client.activity.unauthorized.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation.action;
    requires webfx.extras.panes;
    requires webfx.extras.util.control;
    requires webfx.kit.util;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.session.state.client.fx;

    // Exported packages
    exports one.modality.crm.client.activities.unauthorized;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.client.activities.unauthorized.UnauthorizedRouting.UnauthorizedUiRoute;

}