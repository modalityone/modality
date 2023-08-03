package one.modality.ecommerce.backoffice.activities.statistics.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class StatisticsRouting {

    private final static String ANY_PATH = "/statistics(/event/:eventId)?";
    private final static String EVENT_PATH = "/statistics/event/:eventId";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static String getEventStatisticsPath(Object eventId) {
        return eventId == null ? "/statistics" : ModalityRoutingUtil.interpolateEventIdInPath(eventId, EVENT_PATH);
    }

}
