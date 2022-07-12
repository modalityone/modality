// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.icons;
    requires webfx.extras.imagestore;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.stack.com.buscall;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;
    requires webfx.stack.ui.util;

    // Exported packages
    exports org.modality_project.base.client.application;

    // Resources packages
    opens images.buddhas;
    opens org.modality_project.base.client.images;

}