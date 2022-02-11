package mongoose.ecommerce.frontoffice.activities.person.routing;

import mongoose.base.client.util.routing.MongooseRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class PersonRouting {

    private final static String PATH = "/book/event/:eventId/person";

    public static String getPath() {
        return PATH;
    }

    public static String getPersonPath(Object eventId) {
        return MongooseRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
