// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.base.server.sessioncloser {

    // Direct dependencies modules
    requires webfx.framework.server.push;
    requires webfx.framework.shared.orm.datasourcemodelservice;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.boot;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.submit;

    // Exported packages
    exports mongoose.base.server.jobs.sessioncloser;

    // Provided services
    provides dev.webfx.platform.shared.services.boot.spi.ApplicationJob with mongoose.base.server.jobs.sessioncloser.MongooseServerUnresponsiveClientSessionCloserJob;

}