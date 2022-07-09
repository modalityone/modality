package org.modality_project.hotel.backoffice.activities.roomsgraphic.routing;

import org.modality_project.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class RoomsGraphicRouting {

    private final static String PATH = "/rooms-graphic/event/:eventId";

    public static String getPath() {
        return PATH;
    }

    public static String getEventPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
