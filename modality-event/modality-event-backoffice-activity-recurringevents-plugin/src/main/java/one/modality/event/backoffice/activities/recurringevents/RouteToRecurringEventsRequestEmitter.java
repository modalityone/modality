package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.event.backoffice.operations.routes.recurringevents.RouteToRecurringEventsRequest;

public final class RouteToRecurringEventsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToRecurringEventsRequest(context.getHistory());
    }
}