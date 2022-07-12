package org.modality_project.event.frontoffice.operations.startbooking;

import org.modality_project.event.frontoffice.activities.startbooking.routing.StartBookingRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToStartBookingRequest extends RoutePushRequest {

    public RouteToStartBookingRequest(Object eventId, BrowsingHistory history) {
        super(StartBookingRouting.getStartBookingPath(eventId), history);
    }

}
