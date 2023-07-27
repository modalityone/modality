// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server2018.sessioncloser {

    // Direct dependencies modules
    requires webfx.platform.boot;
    requires webfx.platform.console;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.push.server;

    // Exported packages
    exports one.modality.base.server2018.jobs.sessioncloser;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.base.server2018.jobs.sessioncloser.ModalityServerUnresponsiveClientSessionCloserJob;

}