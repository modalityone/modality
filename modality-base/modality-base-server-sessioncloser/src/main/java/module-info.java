// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.sessioncloser {

    // Direct dependencies modules
    requires webfx.platform.boot;
    requires webfx.platform.console;
    requires webfx.stack.async;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.push.server;

    // Exported packages
    exports org.modality_project.base.server.jobs.sessioncloser;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with org.modality_project.base.server.jobs.sessioncloser.ModalityServerUnresponsiveClientSessionCloserJob;

}