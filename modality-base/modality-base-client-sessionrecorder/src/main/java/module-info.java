// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.sessionrecorder {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires modality.crm.client.authn;
    requires webfx.framework.client.push;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.orm.datasourcemodelservice;
    requires webfx.framework.shared.orm.entity;
    requires webfx.kit.launcher;
    requires webfx.platform.client.storage;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.boot;
    requires webfx.platform.shared.bus;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.submit;

    // Exported packages
    exports org.modality_project.base.client.jobs.sessionrecorder;

    // Provided services
    provides dev.webfx.platform.shared.services.boot.spi.ApplicationJob with org.modality_project.base.client.jobs.sessionrecorder.ClientSessionRecorderJob;

}