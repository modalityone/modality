package org.modality_project.ecommerce.backoffice.operations.routes.income;

import org.modality_project.ecommerce.backoffice.activities.income.routing.IncomeRouting;
import dev.webfx.framework.client.operations.route.RoutePushRequest;
import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToIncomeRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToIncome";

    public RouteToIncomeRequest(Object eventId, BrowsingHistory history) {
        super(IncomeRouting.getEventIncomePath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
