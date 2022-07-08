package org.modality_project.ecommerce.backoffice.activities.statements.routing;

import org.modality_project.base.client.util.routing.MongooseRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class StatementsRouting {

    private final static String PATH = "/statements/event/:eventId";

    public static String getPath() {
        return PATH;
    }

    public static String getPaymentsPath(Object eventId) {
        return MongooseRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
