// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.geoimport {

    // Direct dependencies modules
    requires modality.base.shared.entities;
    requires webfx.platform.ast;
    requires webfx.platform.boot;
    requires webfx.platform.console;
    requires webfx.platform.fetch.ast.json;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.base.server.jobs.geoimport;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.base.server.jobs.geoimport.GeoImportJob;

}