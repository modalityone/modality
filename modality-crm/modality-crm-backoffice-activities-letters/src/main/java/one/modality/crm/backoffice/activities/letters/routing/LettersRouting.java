package one.modality.crm.backoffice.activities.letters.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class LettersRouting {

    private static final String ANY_PATH = "/letters(/organization/:organizationId)?(/event/:eventId)?";
    private static final String EVENT_PATH = "/letters/event/:eventId";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static String getEventLettersPath(Object eventId) {
        return eventId == null ? "/letters" : ModalityRoutingUtil.interpolateEventIdInPath(eventId, EVENT_PATH);
    }

}
