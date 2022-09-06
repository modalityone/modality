package one.modality.crm.backoffice.operations.routes.users;

import one.modality.crm.backoffice.activities.users.routing.UsersRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

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
