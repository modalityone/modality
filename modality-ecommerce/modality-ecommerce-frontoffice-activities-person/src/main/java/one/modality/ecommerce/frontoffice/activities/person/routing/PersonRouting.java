package one.modality.ecommerce.frontoffice.activities.person.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class PersonRouting {

    private static final String PATH = "/book/event/:eventId/person";

    public static String getPath() {
        return PATH;
    }

    public static String getPersonPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }
}
