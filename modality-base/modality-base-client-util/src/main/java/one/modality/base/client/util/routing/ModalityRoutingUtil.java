package one.modality.base.client.util.routing;

import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.entity.Entities;

/**
 * @author Bruno Salmon
 */
public final class ModalityRoutingUtil {

    public static String interpolateParamInPath(String paramToken, Object paramValue, String path) {
        return paramValue == null ? null : Strings.replaceAll(path, paramToken, paramValue);
    }

    public static String interpolateEventIdInPath(Object eventId, String path) {
        return interpolateParamInPath(":eventId", Entities.getPrimaryKey(eventId), path);
    }

    public static String interpolateOrganizationIdInPath(Object organizationId, String path) {
        return interpolateParamInPath(
                ":organizationId", Entities.getPrimaryKey(organizationId), path);
    }

    public static String interpolateLetterIdInPath(Object letterId, String path) {
        return interpolateParamInPath(":letterId", Entities.getPrimaryKey(letterId), path);
    }

    public static String interpolateCartUuidInPath(Object cartUuid, String path) {
        return interpolateParamInPath(":cartUuid", cartUuid, path);
    }

    public static String interpolateDocumentIdInPath(Object documentId, String path) {
        return interpolateParamInPath(":documentId", Entities.getPrimaryKey(documentId), path);
    }
}
