// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.base.server.systemmetrics.java {

    // Direct dependencies modules
    requires java.base;
    requires jdk.management;
    requires mongoose.base.server.systemmetrics;
    requires mongoose.base.shared.entities;

    // Exported packages
    exports mongoose.base.server.services.systemmetrics.spi.java;

    // Provided services
    provides mongoose.base.server.services.systemmetrics.spi.SystemMetricsServiceProvider with mongoose.base.server.services.systemmetrics.spi.java.JavaSystemMetricsServiceProvider;

}