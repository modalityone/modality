package one.modality.event.frontoffice.activities.booking.process.recurring;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class RecurringEventRouting {

    private final static String PATH = "/booking/recurring-event/:eventId";

    public static String getPath() {
        return PATH;
    }

    public static String getRecurringEventPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }


}
