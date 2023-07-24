package one.modality.event.frontoffice.operations.fees;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;

import one.modality.event.frontoffice.activities.fees.routing.FeesRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToFeesRequest extends RoutePushRequest {

    public RouteToFeesRequest(Object eventId, BrowsingHistory history) {
        super(FeesRouting.getFeesPath(eventId), history);
    }
}
