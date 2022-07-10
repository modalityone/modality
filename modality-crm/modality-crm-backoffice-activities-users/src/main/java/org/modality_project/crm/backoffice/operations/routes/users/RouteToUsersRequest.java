package org.modality_project.crm.backoffice.operations.routes.users;

import org.modality_project.crm.backoffice.activities.users.routing.UsersRouting;
import dev.webfx.stack.framework.client.operations.route.RoutePushRequest;
import dev.webfx.stack.framework.shared.operation.HasOperationCode;
import dev.webfx.stack.platform.windowhistory.spi.BrowsingHistory;

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
