// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.sessioncloser {

    // Direct dependencies modules
    requires webfx.framework.server.push;
    requires webfx.framework.shared.orm.datasourcemodelservice;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.platform.shared.boot;
    requires webfx.platform.shared.log;
    requires webfx.stack.async;
    requires webfx.stack.db.submit;

    // Exported packages
    exports org.modality_project.base.server.jobs.sessioncloser;

    // Provided services
    provides dev.webfx.platform.shared.services.boot.spi.ApplicationJob with org.modality_project.base.server.jobs.sessioncloser.ModalityServerUnresponsiveClientSessionCloserJob;

}