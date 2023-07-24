package one.modality.catering.backoffice.operations.routes.diningareas;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.catering.backoffice.activities.diningareas.routing.DiningAreasRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToDiningAreasRequest extends RoutePushRequest implements HasOperationCode {

  private static final String OPERATION_CODE = "RouteToDiningAreas";

  public RouteToDiningAreasRequest(Object eventId, BrowsingHistory history) {
    super(DiningAreasRouting.getEventPath(eventId), history);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
