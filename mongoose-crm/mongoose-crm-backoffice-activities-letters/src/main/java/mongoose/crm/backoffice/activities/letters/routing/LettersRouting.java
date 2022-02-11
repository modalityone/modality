package mongoose.crm.backoffice.activities.letters.routing;

import mongoose.base.client.util.routing.MongooseRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class LettersRouting {

    private static final String ANY_PATH = "/letters(/organization/:organizationId)?(/event/:eventId)?";
    private static final String EVENT_PATH = "/letters/event/:eventId";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static String getEventLettersPath(Object eventId) {
        return MongooseRoutingUtil.interpolateEventIdInPath(eventId, EVENT_PATH);
    }

}
