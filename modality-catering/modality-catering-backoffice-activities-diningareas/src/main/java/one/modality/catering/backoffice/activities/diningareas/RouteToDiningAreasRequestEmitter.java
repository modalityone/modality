package one.modality.catering.backoffice.activities.diningareas;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

import one.modality.catering.backoffice.operations.routes.diningareas.RouteToDiningAreasRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToDiningAreasRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToDiningAreasRequest(context.getParameter("eventId"), context.getHistory());
    }
}
