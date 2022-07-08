// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.systemmetrics {

    // Direct dependencies modules
    requires java.base;
    requires modality.base.shared.entities;
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
    exports org.modality_project.base.server.jobs.systemmetrics;
    exports org.modality_project.base.server.services.systemmetrics;
    exports org.modality_project.base.server.services.systemmetrics.spi;

    // Used services
    uses org.modality_project.base.server.services.systemmetrics.spi.SystemMetricsServiceProvider;

    // Provided services
    provides dev.webfx.platform.shared.services.boot.spi.ApplicationJob with org.modality_project.base.server.jobs.systemmetrics.SystemMetricsRecorderJob;

}