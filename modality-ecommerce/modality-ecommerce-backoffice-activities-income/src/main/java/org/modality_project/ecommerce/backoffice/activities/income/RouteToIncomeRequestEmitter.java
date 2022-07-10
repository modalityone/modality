package org.modality_project.ecommerce.backoffice.activities.income;

import org.modality_project.ecommerce.backoffice.operations.routes.income.RouteToIncomeRequest;
import dev.webfx.stack.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.stack.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToIncomeRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToIncomeRequest(context.getParameter("eventId"), context.getHistory());
    }
}
