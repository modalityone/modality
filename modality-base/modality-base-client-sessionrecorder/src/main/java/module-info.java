// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.sessionrecorder {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires modality.crm.client.authn;
    requires webfx.kit.launcher;
    requires webfx.platform.boot;
    requires webfx.platform.console;
    requires webfx.platform.storage;
    requires webfx.stack.com.bus;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.entity;
    requires webfx.stack.push.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.base.client.jobs.sessionrecorder;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.base.client.jobs.sessionrecorder.ClientSessionRecorderJob;

}