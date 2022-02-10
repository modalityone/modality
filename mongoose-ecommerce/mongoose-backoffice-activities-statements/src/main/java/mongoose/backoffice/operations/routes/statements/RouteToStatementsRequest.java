package mongoose.backoffice.operations.routes.statements;

import mongoose.backoffice.activities.statements.routing.StatementsRouting;
import dev.webfx.framework.client.operations.route.RoutePushRequest;
import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;

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
