package org.modality_project.base.backoffice.activities.operations;

import org.modality_project.base.backoffice.operations.routes.operations.RouteToOperationsRequest;
import dev.webfx.stack.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.stack.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToOperationsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToOperationsRequest(context.getHistory());
    }
}
