// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server2018.systemmetrics.java {

    // Direct dependencies modules
    requires jdk.management;
    requires modality.base.server2018.systemmetrics;
    requires modality.base.shared.entities;

    // Exported packages
    exports one.modality.base.server2018.services.systemmetrics.spi.java;

    // Provided services
    provides one.modality.base.server2018.services.systemmetrics.spi.SystemMetricsServiceProvider with one.modality.base.server2018.services.systemmetrics.spi.java.JavaSystemMetricsServiceProvider;

}