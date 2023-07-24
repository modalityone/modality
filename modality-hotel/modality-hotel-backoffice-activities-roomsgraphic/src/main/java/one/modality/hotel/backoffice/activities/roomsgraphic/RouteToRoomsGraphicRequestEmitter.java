package one.modality.hotel.backoffice.activities.roomsgraphic;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

import one.modality.hotel.backoffice.operations.routes.roomsgraphic.RouteToRoomsGraphicRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToRoomsGraphicRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToRoomsGraphicRequest(
                context.getParameter("eventId"), context.getHistory());
    }
}
