package org.modality_project.crm.backoffice.activities.authorizations;

import org.modality_project.crm.backoffice.activities.operations.authorizations.RouteToAuthorizationsRequest;
import dev.webfx.stack.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.stack.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToAuthorizationsRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToAuthorizationsRequest(context.getHistory());
    }
}
