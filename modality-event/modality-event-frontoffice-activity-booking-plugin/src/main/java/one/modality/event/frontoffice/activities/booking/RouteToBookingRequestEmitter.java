package one.modality.event.frontoffice.activities.booking;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.event.frontoffice.operations.routes.booking.RouteToBookingRequest;

public class RouteToBookingRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToBookingRequest(context.getHistory());
    }
}