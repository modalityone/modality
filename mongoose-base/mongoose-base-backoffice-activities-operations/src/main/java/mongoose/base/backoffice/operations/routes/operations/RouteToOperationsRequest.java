package mongoose.base.backoffice.operations.routes.operations;


import mongoose.base.backoffice.activities.operations.routing.OperationsRouting;
import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.client.operations.route.RoutePushRequest;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToOperationsRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToOperations";

    public RouteToOperationsRequest(BrowsingHistory history) {
        super(OperationsRouting.getPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
