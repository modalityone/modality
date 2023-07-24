package one.modality.base.backoffice.operations.routes.operations;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.base.backoffice.activities.operations.routing.OperationsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToOperationsRequest extends RoutePushRequest implements HasOperationCode {

  private static final String OPERATION_CODE = "RouteToOperations";

  public RouteToOperationsRequest(BrowsingHistory history) {
    super(OperationsRouting.getPath(), history);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
