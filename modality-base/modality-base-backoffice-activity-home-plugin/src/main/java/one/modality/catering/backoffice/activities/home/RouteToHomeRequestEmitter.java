package one.modality.catering.backoffice.activities.home;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.catering.backoffice.operations.routes.home.RouteToHomeRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToHomeRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToHomeRequest(context.getHistory());
    }
}
