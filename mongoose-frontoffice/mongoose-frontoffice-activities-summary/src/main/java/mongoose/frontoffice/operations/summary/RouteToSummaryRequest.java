package mongoose.frontoffice.operations.summary;

import mongoose.frontoffice.activities.summary.routing.SummaryRouting;
import dev.webfx.framework.client.operations.route.RoutePushRequest;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToSummaryRequest extends RoutePushRequest {

    public RouteToSummaryRequest(Object eventId, BrowsingHistory history) {
        super(SummaryRouting.getSummaryPath(eventId), history);
    }

}
