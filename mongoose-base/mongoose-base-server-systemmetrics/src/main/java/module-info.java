// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.base.server.systemmetrics {

    // Direct dependencies modules
    requires java.base;
    requires mongoose.base.shared.entities;
    requires webfx.framework.shared.orm.datasourcemodelservice;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.framework.shared.orm.entity;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.boot;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.scheduler;
    requires webfx.platform.shared.submit;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.base.server.jobs.systemmetrics;
    exports mongoose.base.server.services.systemmetrics;
    exports mongoose.base.server.services.systemmetrics.spi;

    // Used services
    uses mongoose.base.server.services.systemmetrics.spi.SystemMetricsServiceProvider;

    // Provided services
    provides dev.webfx.platform.shared.services.boot.spi.ApplicationJob with mongoose.base.server.jobs.systemmetrics.SystemMetricsRecorderJob;

}