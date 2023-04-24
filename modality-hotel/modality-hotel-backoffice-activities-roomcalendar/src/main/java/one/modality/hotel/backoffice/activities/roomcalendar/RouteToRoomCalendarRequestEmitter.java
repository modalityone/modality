package one.modality.hotel.backoffice.activities.roomcalendar;

import one.modality.hotel.backoffice.operations.routes.roomcalendar.RouteToRoomCalendarRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToRoomCalendarRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToRoomCalendarRequest(context.getHistory());
    }
}
