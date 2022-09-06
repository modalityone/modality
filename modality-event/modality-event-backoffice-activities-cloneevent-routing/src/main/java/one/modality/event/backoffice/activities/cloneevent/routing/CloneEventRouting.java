package one.modality.event.backoffice.activities.cloneevent.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class CloneEventRouting {

    private static final String PATH = "/event/:eventId/clone";

    public static String getPath() {
        return PATH;
    }

    public static String getCloneEventPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, getPath());
    }

}
