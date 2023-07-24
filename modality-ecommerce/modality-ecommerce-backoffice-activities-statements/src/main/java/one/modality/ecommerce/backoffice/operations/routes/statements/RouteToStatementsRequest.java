package one.modality.ecommerce.backoffice.operations.routes.statements;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.ecommerce.backoffice.activities.statements.routing.StatementsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToStatementsRequest extends RoutePushRequest implements HasOperationCode {

  private static final String OPERATION_CODE = "RouteToStatements";

  public RouteToStatementsRequest(Object eventId, BrowsingHistory history) {
    super(StatementsRouting.getPaymentsPath(eventId), history);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
