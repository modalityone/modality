// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Define a generic Modality JavaFX application (common code to the back-office and front-office).
 */
module modality.base.client.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.css;
    requires modality.base.client.icons;
    requires webfx.extras.imagestore;
    requires webfx.extras.theme;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.scene;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.kit.util.scene;
    requires webfx.platform.uischeduler;
    requires transitive webfx.platform.util;
    requires webfx.stack.authn.logout.client;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.datasourcemodel.service;
    requires transitive webfx.stack.orm.domainmodel.activity;
    requires transitive webfx.stack.routing.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.base.client.application;

    // Resources packages
    opens images.buddhas;
    opens one.modality.base.client.images;

}