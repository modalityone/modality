// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.frontoffice.activity.userprofile.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.shared.entities;
    requires modality.crm.client.activity.magiclink.plugin;
    requires modality.crm.client.authn.fx;
    requires modality.crm.frontoffice.activity.createaccount.plugin;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.styles.materialdesign;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.windowhistory;
    requires webfx.platform.windowlocation;
    requires webfx.stack.authn;
    requires webfx.stack.authn.login.ui;
    requires webfx.stack.authn.login.ui.gateway.password.plugin;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.validation;

    // Exported packages
    exports one.modality.crm.frontoffice.activities.userprofile;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.frontoffice.activities.userprofile.UserProfileRouting.UserProfileUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.crm.frontoffice.activities.userprofile.UserProfileRouting.RouteToUserProfileRequestEmitter;

}