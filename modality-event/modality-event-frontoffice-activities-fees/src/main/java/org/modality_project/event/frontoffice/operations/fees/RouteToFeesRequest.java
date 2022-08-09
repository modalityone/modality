package org.modality_project.event.frontoffice.operations.fees;

import org.modality_project.event.frontoffice.activities.fees.routing.FeesRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToFeesRequest extends RoutePushRequest {

    public RouteToFeesRequest(Object eventId, BrowsingHistory history) {
        super(FeesRouting.getFeesPath(eventId), history);
    }

}
