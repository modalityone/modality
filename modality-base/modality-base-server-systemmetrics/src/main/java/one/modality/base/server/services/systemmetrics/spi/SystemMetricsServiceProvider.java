package one.modality.base.server.services.systemmetrics.spi;

import one.modality.base.shared.services.systemmetrics.SystemMetrics;

/**
 * @author Bruno Salmon
 */
public interface SystemMetricsServiceProvider {

  void takeSystemMetricsSnapshot(SystemMetrics systemMetrics);
}
