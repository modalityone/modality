package org.modality_project.base.client.navigationarrows;

import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteForwardRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteForwardRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteForwardRequest(context.getHistory());
    }
}
