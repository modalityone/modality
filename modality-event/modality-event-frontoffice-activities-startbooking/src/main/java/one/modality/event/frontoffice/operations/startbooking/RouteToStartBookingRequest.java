package one.modality.event.frontoffice.operations.startbooking;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;

import one.modality.event.frontoffice.activities.startbooking.routing.StartBookingRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToStartBookingRequest extends RoutePushRequest {

    public RouteToStartBookingRequest(Object eventId, BrowsingHistory history) {
        super(StartBookingRouting.getStartBookingPath(eventId), history);
    }
}
