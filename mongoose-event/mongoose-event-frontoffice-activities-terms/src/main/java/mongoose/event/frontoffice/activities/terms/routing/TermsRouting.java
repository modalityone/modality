package mongoose.event.frontoffice.activities.terms.routing;

import mongoose.base.client.util.routing.MongooseRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class TermsRouting {

    private final static String PATH = "/book/event/:eventId/terms";

    public static String getPath() {
        return PATH;
    }

    public static String getTermsPath(Object eventId) {
        return MongooseRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
