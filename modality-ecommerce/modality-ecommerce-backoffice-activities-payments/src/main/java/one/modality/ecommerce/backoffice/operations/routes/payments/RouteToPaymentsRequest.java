package one.modality.ecommerce.backoffice.operations.routes.payments;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.ecommerce.backoffice.activities.payments.routing.PaymentsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToPaymentsRequest extends RoutePushRequest implements HasOperationCode {

  private static final String OPERATION_CODE = "RouteToPayments";

  public RouteToPaymentsRequest(Object eventId, BrowsingHistory history) {
    super(PaymentsRouting.getPaymentsPath(eventId), history);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
