package mongoose.backend.activities.operations.authorizations;


import mongoose.backend.activities.authorizations.routing.AuthorizationsRouting;
import dev.webfx.framework.client.operations.route.RoutePushRequest;
import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToAuthorizationsRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToAuthorizations";

    public RouteToAuthorizationsRequest(BrowsingHistory history) {
        super(AuthorizationsRouting.getPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
