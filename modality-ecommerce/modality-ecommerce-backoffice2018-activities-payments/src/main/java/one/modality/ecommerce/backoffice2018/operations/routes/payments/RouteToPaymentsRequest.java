package one.modality.ecommerce.backoffice2018.operations.routes.payments;

import one.modality.ecommerce.backoffice2018.activities.payments.routing.PaymentsRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToPaymentsRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToPayments";

    public RouteToPaymentsRequest(Object eventId, BrowsingHistory history) {
        super(PaymentsRouting.getPaymentsPath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
