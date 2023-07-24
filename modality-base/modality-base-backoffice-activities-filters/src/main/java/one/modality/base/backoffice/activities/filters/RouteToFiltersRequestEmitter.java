package one.modality.base.backoffice.activities.filters;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

import one.modality.base.backoffice.operations.routes.filters.RouteToFiltersRequest;

public final class RouteToFiltersRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToFiltersRequest(context.getHistory());
    }
}
