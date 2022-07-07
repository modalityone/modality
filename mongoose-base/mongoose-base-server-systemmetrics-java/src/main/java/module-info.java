// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.base.server.systemmetrics.java {

    // Direct dependencies modules
    requires jdk.management;
    requires mongoose.base.server.systemmetrics;
    requires mongoose.base.shared.entities;

    // Exported packages
    exports org.modality_project.base.server.services.systemmetrics.spi.java;

    // Provided services
    provides org.modality_project.base.server.services.systemmetrics.spi.SystemMetricsServiceProvider with org.modality_project.base.server.services.systemmetrics.spi.java.JavaSystemMetricsServiceProvider;

}