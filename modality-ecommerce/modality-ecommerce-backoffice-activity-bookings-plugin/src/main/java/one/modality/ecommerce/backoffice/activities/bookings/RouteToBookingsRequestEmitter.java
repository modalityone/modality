package one.modality.ecommerce.backoffice.activities.bookings;

import one.modality.ecommerce.backoffice.operations.routes.bookings.RouteToBookingsRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToBookingsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToBookingsRequest(context.getParameter("eventId"), context.getHistory());
    }
}
