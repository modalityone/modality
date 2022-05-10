package mongoose.base.backoffice.activities.filters;

import mongoose.base.backoffice.operations.routes.filters.RouteToFiltersRequest;
import dev.webfx.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.framework.shared.router.auth.authz.RouteRequest;

public final class RouteToFiltersRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToFiltersRequest(context.getParameter("eventId"), context.getHistory());
    }
}
