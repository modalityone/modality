package org.modality_project.ecommerce.backoffice.activities.bookings;

import org.modality_project.ecommerce.backoffice.operations.routes.bookings.RouteToBookingsRequest;
import dev.webfx.stack.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.stack.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToBookingsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToBookingsRequest(context.getParameter("eventId"), context.getHistory());
    }
}
