package one.modality.base.backoffice.activities.operations;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

import one.modality.base.backoffice.operations.routes.operations.RouteToOperationsRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToOperationsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToOperationsRequest(context.getHistory());
    }
}
