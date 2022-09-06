package one.modality.ecommerce.backoffice.activities.statistics.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class StatisticsRouting {

    private final static String PATH = "/statistics/event/:eventId";

    public static String getPath() {
        return PATH;
    }

    public static String getEventStatisticsPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
