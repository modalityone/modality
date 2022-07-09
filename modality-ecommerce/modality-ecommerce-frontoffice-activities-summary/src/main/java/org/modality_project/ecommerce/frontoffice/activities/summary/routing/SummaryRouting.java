package org.modality_project.ecommerce.frontoffice.activities.summary.routing;

import org.modality_project.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class SummaryRouting {

    private final static String PATH = "/book/event/:eventId/summary";

    public static String getPath() {
        return PATH;
    }

    public static String getSummaryPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
