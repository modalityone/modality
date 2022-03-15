package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.framework.shared.router.auth.authz.RouteRequest;
import mongoose.ecommerce.backoffice.operations.routes.moneyflows.RouteToMoneyFlowsRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToMoneyFlowsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToMoneyFlowsRequest(context.getParameter("organizationId"), context.getHistory());
    }
}
