package mongoose.backend.activities.statements;

import mongoose.backend.operations.routes.statements.RouteToStatementsRequest;
import dev.webfx.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToStatementsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToStatementsRequest(context.getParameter("eventId"), context.getHistory());
    }
}
