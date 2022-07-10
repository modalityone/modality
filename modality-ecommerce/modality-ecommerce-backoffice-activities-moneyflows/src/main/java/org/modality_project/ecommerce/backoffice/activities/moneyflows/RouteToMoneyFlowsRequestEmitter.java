package org.modality_project.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.stack.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.stack.framework.shared.router.auth.authz.RouteRequest;
import org.modality_project.ecommerce.backoffice.operations.routes.moneyflows.RouteToMoneyFlowsRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToMoneyFlowsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToMoneyFlowsRequest(context.getParameter("organizationId"), context.getHistory());
    }
}
