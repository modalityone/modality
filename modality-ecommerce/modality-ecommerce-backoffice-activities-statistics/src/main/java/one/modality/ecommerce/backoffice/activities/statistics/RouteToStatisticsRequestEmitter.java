package one.modality.ecommerce.backoffice.activities.statistics;

import one.modality.ecommerce.backoffice.operations.routes.statistics.RouteToStatisticsRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToStatisticsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToStatisticsRequest(context.getParameter("eventId"), context.getHistory());
    }
}
