package mongoose.crm.backoffice.operations.routes.users;

import mongoose.crm.backoffice.activities.users.routing.UsersRouting;
import dev.webfx.framework.client.operations.route.RoutePushRequest;
import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToUsersRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToUsers";

    public RouteToUsersRequest(BrowsingHistory history) {
        super(UsersRouting.getPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
