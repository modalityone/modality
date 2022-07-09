package org.modality_project.crm.backoffice.activities.letter.routing;

import org.modality_project.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class LetterRouting {

    private final static String PATH = "/letter/:letterId";

    public static String getPath() {
        return PATH;
    }

    public static String getEditLetterPath(Object letterId) {
        return ModalityRoutingUtil.interpolateLetterIdInPath(letterId, PATH);
    }

}
