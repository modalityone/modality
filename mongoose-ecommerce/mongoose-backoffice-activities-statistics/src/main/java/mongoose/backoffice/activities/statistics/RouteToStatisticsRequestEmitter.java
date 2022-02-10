package mongoose.backoffice.activities.statistics;

import mongoose.backoffice.operations.routes.statistics.RouteToStatisticsRequest;
import dev.webfx.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToStatisticsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToStatisticsRequest(context.getParameter("eventId"), context.getHistory());
    }
}
