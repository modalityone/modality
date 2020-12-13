package mongoose.backend.activities.organizations;

import mongoose.backend.operations.routes.organizations.RouteToOrganizationsRequest;
import dev.webfx.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToOrganizationsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToOrganizationsRequest(context.getHistory());
    }
}
