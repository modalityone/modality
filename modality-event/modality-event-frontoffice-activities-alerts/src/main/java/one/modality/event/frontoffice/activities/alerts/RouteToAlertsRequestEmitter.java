package one.modality.event.frontoffice.activities.alerts;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.event.frontoffice.operations.routes.alerts.RouteToAlertsRequest;

public class RouteToAlertsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToAlertsRequest(context.getHistory());
    }
}