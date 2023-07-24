package one.modality.event.backoffice.activities.events.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class EventsRouting {

    private static final String ANY_PATH = "/events(/organization/:organizationId)?";
    private static final String ALL_EVENTS_PATH = "/events";
    private static final String ORGANIZATION_PATH = "/events/organization/:organizationId";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static String getAllEventsPath() {
        return ALL_EVENTS_PATH;
    }

    public static String getOrganizationEventsPath(Object organizationId) {
        return ModalityRoutingUtil.interpolateOrganizationIdInPath(
                organizationId, ORGANIZATION_PATH);
    }
}
