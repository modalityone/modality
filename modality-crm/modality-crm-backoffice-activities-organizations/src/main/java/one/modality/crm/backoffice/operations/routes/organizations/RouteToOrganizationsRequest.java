package one.modality.crm.backoffice.operations.routes.organizations;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;

import one.modality.crm.backoffice.activities.organizations.routing.OrganizationsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToOrganizationsRequest extends RoutePushRequest
        implements HasOperationCode {

    private static final String OPERATION_CODE = "RouteToOrganizations";

    public RouteToOrganizationsRequest(BrowsingHistory history) {
        super(OrganizationsRouting.getPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
