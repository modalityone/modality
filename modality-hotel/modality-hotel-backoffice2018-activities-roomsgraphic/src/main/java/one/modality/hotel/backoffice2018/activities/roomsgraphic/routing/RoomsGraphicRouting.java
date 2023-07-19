package one.modality.hotel.backoffice2018.activities.roomsgraphic.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class RoomsGraphicRouting {

    private final static String ANY_PATH = "/rooms-graphic(/event/:eventId)?";
    private final static String EVENT_PATH = "/rooms-graphic/event/:eventId";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static String getEventPath(Object eventId) {
        return eventId == null ? "/rooms-graphic" : ModalityRoutingUtil.interpolateEventIdInPath(eventId, EVENT_PATH);
    }

}
