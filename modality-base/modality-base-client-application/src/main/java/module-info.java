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
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.operationaction;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.datasourcemodelservice;
    requires webfx.framework.shared.router;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.stack.com.buscall;

    // Exported packages
    exports org.modality_project.base.client.application;

    // Resources packages
    opens images.buddhas;
    opens org.modality_project.base.client.images;

}