package one.modality.ecommerce.backoffice2018.activities.payments.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class PaymentsRouting {

    private final static String PATH = "/payments/event/:eventId";

    public static String getPath() {
        return PATH;
    }

    public static String getPaymentsPath(Object eventId) {
        return ModalityRoutingUtil.interpolateEventIdInPath(eventId, PATH);
    }

}
