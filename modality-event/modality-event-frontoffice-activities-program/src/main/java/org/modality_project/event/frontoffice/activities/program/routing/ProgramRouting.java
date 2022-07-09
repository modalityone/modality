package org.modality_project.event.frontoffice.activities.program.routing;

import org.modality_project.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class ProgramRouting {

    private final static String PATH = "/book/event/:eventId/program";

    public static String getPath() {
        return PATH;
    }

    public static String getProgramPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
