package mongoose.backend.activities.income;

import mongoose.backend.operations.routes.income.RouteToIncomeRequest;
import dev.webfx.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToIncomeRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToIncomeRequest(context.getParameter("eventId"), context.getHistory());
    }
}
