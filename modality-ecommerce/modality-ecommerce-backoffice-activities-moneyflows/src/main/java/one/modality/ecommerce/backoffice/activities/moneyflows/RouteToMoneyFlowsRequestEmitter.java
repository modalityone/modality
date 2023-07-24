package one.modality.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

import one.modality.ecommerce.backoffice.operations.routes.moneyflows.RouteToMoneyFlowsRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToMoneyFlowsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToMoneyFlowsRequest(
                context.getParameter("organizationId"), context.getHistory());
    }
}
