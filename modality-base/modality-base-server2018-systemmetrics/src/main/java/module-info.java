// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server2018.systemmetrics {

    // Direct dependencies modules
    requires java.base;
    requires modality.base.shared.entities;
    requires webfx.platform.boot;
    requires webfx.platform.console;
    requires webfx.platform.scheduler;
    requires webfx.platform.util;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.base.server2018.jobs.systemmetrics;
    exports one.modality.base.server2018.services.systemmetrics;
    exports one.modality.base.server2018.services.systemmetrics.spi;

    // Used services
    uses one.modality.base.server2018.services.systemmetrics.spi.SystemMetricsServiceProvider;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.base.server2018.jobs.systemmetrics.SystemMetricsRecorderJob;

}