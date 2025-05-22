// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Define a generic Modality JavaFX application (common code to the back-office and front-office).
 */
module modality.base.client.application {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.icons;
    requires modality.base.client.profile.fx;
    requires webfx.extras.theme;
    requires webfx.extras.time.format;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.scene;
    requires webfx.kit.util;
    requires webfx.kit.util.scene;
    requires webfx.platform.boot;
    requires transitive webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.authn.logout.client;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.datasourcemodel.service;
    requires transitive webfx.stack.orm.domainmodel.activity;
    requires transitive webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.session;
    requires webfx.stack.session.state.client.fx;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.base.client.application;

    // Resources packages
    opens images.buddhas;
    opens one.modality.base.client.images;

}