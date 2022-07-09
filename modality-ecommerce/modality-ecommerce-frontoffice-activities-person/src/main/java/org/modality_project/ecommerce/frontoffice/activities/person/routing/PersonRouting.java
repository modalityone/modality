package org.modality_project.ecommerce.frontoffice.activities.person.routing;

import org.modality_project.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class PersonRouting {

    private final static String PATH = "/book/event/:eventId/person";

    public static String getPath() {
        return PATH;
    }

    public static String getPersonPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
