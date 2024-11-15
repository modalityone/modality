// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.activity.magiclink.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires webfx.platform.windowhistory;
    requires webfx.stack.authn.login.ui;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.crm.activities.magiclink;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.activities.magiclink.MagicLinkRouting.MagicLinkUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.crm.activities.magiclink.MagicLinkRouting.RouteToMagicLinkRequestEmitter;

}