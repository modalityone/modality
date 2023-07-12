// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.newsimport {

    // Direct dependencies modules
    requires java.base;
    requires modality.base.shared.entities;
    requires webfx.platform.async;
    requires webfx.platform.boot;
    requires webfx.platform.console;
    requires webfx.platform.fetch;
    requires webfx.platform.json;
    requires webfx.platform.scheduler;
    requires webfx.platform.util;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.base.server.jobs.newsimport;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.base.server.jobs.newsimport.NewsImportJob;

}