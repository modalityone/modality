package org.modality_project.crm.backoffice.activities.letters;

import org.modality_project.crm.backoffice.operations.routes.letters.RouteToLettersRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToLettersRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToLettersRequest(context.getParameter("eventId"), context.getHistory());
    }
}
