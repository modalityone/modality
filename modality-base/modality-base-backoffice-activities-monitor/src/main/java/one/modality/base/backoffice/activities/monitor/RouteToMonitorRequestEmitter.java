package one.modality.base.backoffice.activities.monitor;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

import one.modality.base.backoffice.operations.routes.monitor.RouteToMonitorRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToMonitorRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToMonitorRequest(context.getHistory());
    }
}
