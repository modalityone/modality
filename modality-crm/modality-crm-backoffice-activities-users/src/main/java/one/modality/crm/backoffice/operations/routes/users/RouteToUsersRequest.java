package one.modality.crm.backoffice.operations.routes.users;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;

import one.modality.crm.backoffice.activities.users.routing.UsersRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToUsersRequest extends RoutePushRequest implements HasOperationCode {

    private static final String OPERATION_CODE = "RouteToUsers";

    public RouteToUsersRequest(BrowsingHistory history) {
        super(UsersRouting.getPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
