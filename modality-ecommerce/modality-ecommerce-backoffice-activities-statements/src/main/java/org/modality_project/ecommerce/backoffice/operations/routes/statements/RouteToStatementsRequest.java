package org.modality_project.ecommerce.backoffice.operations.routes.statements;

import org.modality_project.ecommerce.backoffice.activities.statements.routing.StatementsRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToStatementsRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToStatements";

    public RouteToStatementsRequest(Object eventId, BrowsingHistory history) {
        super(StatementsRouting.getPaymentsPath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
