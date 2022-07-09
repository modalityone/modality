package org.modality_project.event.backoffice.activities.events.routing;

import org.modality_project.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class EventsRouting {

    private final static String ANY_PATH = "/events(/organization/:organizationId)?";
    private final static String ALL_EVENTS_PATH = "/events";
    private final static String ORGANIZATION_PATH = "/events/organization/:organizationId";


    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static String getAllEventsPath() {
        return ALL_EVENTS_PATH;
    }

    public static String getOrganizationEventsPath(Object organizationId) {
        return ModalityRoutingUtil.interpolateOrganizationIdInPath(organizationId, ORGANIZATION_PATH);
    }

}
