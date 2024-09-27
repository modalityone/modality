// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.magiclink.application {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires webfx.extras.styles.bootstrap;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.windowlocation;
    requires webfx.stack.authn;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;

    // Exported packages
    exports one.modality.crm.magiclink.application;

    // Provided services
    provides javafx.application.Application with one.modality.crm.magiclink.application.MagicLinkApplication;

}