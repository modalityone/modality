package one.modality.event.frontoffice.activities.booking.process.event;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class BookEventRouting {

    private final static String PATH = "/booking/event/:eventId";

    public static String getPath() {
        return PATH;
    }

    public static String getBookEventPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }


}
