package one.modality.hotel.backoffice.operations.routes.roomsgraphic;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.hotel.backoffice.activities.roomsgraphic.routing.RoomsGraphicRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToRoomsGraphicRequest extends RoutePushRequest implements HasOperationCode {

  private static final String OPERATION_CODE = "RouteToRoomsGraphic";

  public RouteToRoomsGraphicRequest(Object eventId, BrowsingHistory history) {
    super(RoomsGraphicRouting.getEventPath(eventId), history);
  }

  @Override
  public Object getOperationCode() {
    return OPERATION_CODE;
  }
}
