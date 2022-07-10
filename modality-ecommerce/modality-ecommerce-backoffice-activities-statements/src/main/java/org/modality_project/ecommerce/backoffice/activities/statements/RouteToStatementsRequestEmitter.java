package org.modality_project.ecommerce.backoffice.activities.statements;

import org.modality_project.ecommerce.backoffice.operations.routes.statements.RouteToStatementsRequest;
import dev.webfx.stack.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.stack.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToStatementsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToStatementsRequest(context.getParameter("eventId"), context.getHistory());
    }
}
