package mongoose.event.frontoffice.activities.program.routing;

import mongoose.base.client.util.routing.MongooseRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class ProgramRouting {

    private final static String PATH = "/book/event/:eventId/program";

    public static String getPath() {
        return PATH;
    }

    public static String getProgramPath(Object eventId) {
        return MongooseRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
