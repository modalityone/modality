package org.modality_project.crm.backoffice.operations.routes.organizations;


import org.modality_project.crm.backoffice.activities.organizations.routing.OrganizationsRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

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
