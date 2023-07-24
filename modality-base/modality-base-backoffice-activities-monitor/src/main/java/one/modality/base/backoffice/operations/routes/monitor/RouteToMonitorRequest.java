package one.modality.base.backoffice.operations.routes.monitor;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.base.backoffice.activities.monitor.routing.MonitorRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToMonitorRequest extends RoutePushRequest implements HasOperationCode {

  private static final String OPERATION_CODE = "RouteToMonitor";

  public RouteToMonitorRequest(BrowsingHistory history) {
    super(MonitorRouting.getPath(), history);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
