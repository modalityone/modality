package org.modality_project.hotel.backoffice.activities.roomsgraphic;

import org.modality_project.hotel.backoffice.operations.routes.roomsgraphic.RouteToRoomsGraphicRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToRoomsGraphicRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToRoomsGraphicRequest(context.getParameter("eventId"), context.getHistory());
    }
}
