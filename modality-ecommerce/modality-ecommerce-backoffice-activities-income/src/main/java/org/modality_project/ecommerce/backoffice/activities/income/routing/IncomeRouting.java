package org.modality_project.ecommerce.backoffice.activities.income.routing;

import org.modality_project.base.client.util.routing.MongooseRoutingUtil;

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
