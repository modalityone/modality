package one.modality.base.client.activities.console;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.base.client.operations.routes.RouteToConsoleRequest;

/**
 * @author Bruno Salmon
 */
public class RouteToConsoleRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToConsoleRequest(context.getHistory());
    }

}
