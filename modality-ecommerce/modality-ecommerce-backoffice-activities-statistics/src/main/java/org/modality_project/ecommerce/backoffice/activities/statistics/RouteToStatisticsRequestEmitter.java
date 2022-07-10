package org.modality_project.ecommerce.backoffice.activities.statistics;

import org.modality_project.ecommerce.backoffice.operations.routes.statistics.RouteToStatisticsRequest;
import dev.webfx.stack.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.stack.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToStatisticsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToStatisticsRequest(context.getParameter("eventId"), context.getHistory());
    }
}
