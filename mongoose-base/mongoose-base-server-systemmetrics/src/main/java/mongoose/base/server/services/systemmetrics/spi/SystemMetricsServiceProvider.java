package mongoose.base.server.services.systemmetrics.spi;

import mongoose.base.shared.services.systemmetrics.SystemMetrics;

/**
 * @author Bruno Salmon
 */
public interface SystemMetricsServiceProvider {

    void takeSystemMetricsSnapshot(SystemMetrics systemMetrics);

}
