package one.modality.event.frontoffice2018.activities.startbooking.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class StartBookingRouting {

    private final static String PATH = "/book/event/:eventId/start";

    public static String getPath() {
        return PATH;
    }

    public static String getStartBookingPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
