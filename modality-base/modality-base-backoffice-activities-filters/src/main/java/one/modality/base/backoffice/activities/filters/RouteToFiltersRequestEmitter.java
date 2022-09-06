package one.modality.base.backoffice.activities.filters;

import one.modality.base.backoffice.operations.routes.filters.RouteToFiltersRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;

public final class RouteToFiltersRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToFiltersRequest(context.getHistory());
    }
}
