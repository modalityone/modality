package one.modality.ecommerce.backoffice.operations.routes.moneyflows;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.ecommerce.backoffice.activities.moneyflows.routing.MoneyFlowsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToMoneyFlowsRequest extends RoutePushRequest implements HasOperationCode {

  private static final String OPERATION_CODE = "RouteToMoneyFlows";

  public RouteToMoneyFlowsRequest(Object organizationId, BrowsingHistory history) {
    super(MoneyFlowsRouting.getOrganizationIncomePath(organizationId), history);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
