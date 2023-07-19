package one.modality.ecommerce.backoffice2018.activities.statements.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class StatementsRouting {

    private final static String PATH = "/statements/event/:eventId";

    public static String getPath() {
        return PATH;
    }

    public static String getPaymentsPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
