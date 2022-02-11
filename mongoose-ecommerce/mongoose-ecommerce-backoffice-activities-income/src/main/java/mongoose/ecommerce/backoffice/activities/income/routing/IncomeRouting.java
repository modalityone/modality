package mongoose.ecommerce.backoffice.activities.income.routing;

import mongoose.base.client.util.routing.MongooseRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class IncomeRouting {

    private final static String PATH = "/income/event/:eventId";

    public static String getPath() {
        return PATH;
    }

    public static String getEventIncomePath(Object eventId) {
        return MongooseRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
