package org.modality_project.crm.backoffice.activities.organizations;

import org.modality_project.crm.backoffice.operations.routes.organizations.RouteToOrganizationsRequest;
import dev.webfx.stack.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.stack.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToOrganizationsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToOrganizationsRequest(context.getHistory());
    }
}
