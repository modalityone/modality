package mongoose.backend.operations.routes.organizations;


import mongoose.backend.activities.organizations.routing.OrganizationsRouting;
import dev.webfx.framework.client.operations.route.RoutePushRequest;
import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToOrganizationsRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToOrganizations";

    public RouteToOrganizationsRequest(BrowsingHistory history) {
        super(OrganizationsRouting.getPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
