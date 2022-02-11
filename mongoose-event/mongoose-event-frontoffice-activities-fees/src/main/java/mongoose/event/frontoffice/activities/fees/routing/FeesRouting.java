package mongoose.event.frontoffice.activities.fees.routing;

import mongoose.base.client.util.routing.MongooseRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class FeesRouting {

    private final static String PATH = "/book/event/:eventId/fees";

    public static String getPath() {
        return PATH;
    }

    public static String getFeesPath(Object eventId) {
        return MongooseRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
