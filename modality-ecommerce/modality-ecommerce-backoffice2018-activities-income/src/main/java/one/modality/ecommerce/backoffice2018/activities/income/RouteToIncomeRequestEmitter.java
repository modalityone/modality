package one.modality.ecommerce.backoffice2018.activities.income;

import one.modality.ecommerce.backoffice2018.operations.routes.income.RouteToIncomeRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToIncomeRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToIncomeRequest(context.getParameter("eventId"), context.getHistory());
    }
}
