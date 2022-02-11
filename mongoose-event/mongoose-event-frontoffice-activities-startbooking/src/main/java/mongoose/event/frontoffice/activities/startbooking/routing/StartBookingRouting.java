package mongoose.event.frontoffice.activities.startbooking.routing;

import mongoose.base.client.util.routing.MongooseRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class StartBookingRouting {

    private final static String PATH = "/book/event/:eventId/start";

    public static String getPath() {
        return PATH;
    }

    public static String getStartBookingPath(Object eventId) {
        return MongooseRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
