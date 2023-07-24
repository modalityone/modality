package one.modality.ecommerce.backoffice.activities.income.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class IncomeRouting {

    private static final String PATH = "/income/event/:eventId";

    public static String getPath() {
        return PATH;
    }

    public static String getEventIncomePath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }
}
