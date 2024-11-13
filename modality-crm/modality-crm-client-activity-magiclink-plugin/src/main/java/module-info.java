// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.activity.magiclink.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.bootstrap;
    requires modality.base.client.icons;
    requires modality.crm.client.magiclink.application;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.webtext;
    requires webfx.kit.util;
    requires webfx.platform.conf;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.windowhistory;
    requires webfx.stack.authn;
    requires webfx.stack.authn.login.ui.gateway.password.plugin;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.crm.activities.magiclink;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.activities.magiclink.MagicLinkRouting.MagicLinkUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.crm.activities.magiclink.MagicLinkRouting.RouteToMagicLinkRequestEmitter;

}