package one.modality.ecommerce.frontoffice.operations.summary;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import one.modality.ecommerce.frontoffice.activities.summary.routing.SummaryRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToSummaryRequest extends RoutePushRequest {

  public RouteToSummaryRequest(Object eventId, BrowsingHistory history) {
    super(SummaryRouting.getSummaryPath(eventId), history);
  }
}
