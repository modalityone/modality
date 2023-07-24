package one.modality.ecommerce.backoffice.operations.routes.income;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;

import one.modality.ecommerce.backoffice.activities.income.routing.IncomeRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToIncomeRequest extends RoutePushRequest implements HasOperationCode {

    private static final String OPERATION_CODE = "RouteToIncome";

    public RouteToIncomeRequest(Object eventId, BrowsingHistory history) {
        super(IncomeRouting.getEventIncomePath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
