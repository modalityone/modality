package org.modality_project.ecommerce.backoffice.operations.routes.moneyflows;

import dev.webfx.stack.framework.client.operations.route.RoutePushRequest;
import dev.webfx.stack.framework.shared.operation.HasOperationCode;
import dev.webfx.stack.platform.windowhistory.spi.BrowsingHistory;
import org.modality_project.ecommerce.backoffice.activities.moneyflows.routing.MoneyFlowsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToMoneyFlowsRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToIncome";

    public RouteToMoneyFlowsRequest(Object organizationId, BrowsingHistory history) {
        super(MoneyFlowsRouting.getOrganizationIncomePath(organizationId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
