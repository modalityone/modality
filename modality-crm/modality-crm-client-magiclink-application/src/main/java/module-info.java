// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.magiclink.application {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.i18n;
    requires modality.base.client.icons;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.webtext;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.windowlocation;
    requires webfx.stack.authn;
    requires webfx.stack.authn.login.ui;
    requires webfx.stack.authn.login.ui.gateway.password.plugin;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.crm.magiclink.application;

    // Provided services
    provides javafx.application.Application with one.modality.crm.magiclink.application.MagicLinkApplication;

}