package mongoose.backend.activities.operations;

import mongoose.backend.operations.routes.operations.RouteToOperationsRequest;
import dev.webfx.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToOperationsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToOperationsRequest(context.getHistory());
    }
}
