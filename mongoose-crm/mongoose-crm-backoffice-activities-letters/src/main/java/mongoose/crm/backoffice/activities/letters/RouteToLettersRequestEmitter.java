package mongoose.crm.backoffice.activities.letters;

import mongoose.crm.backoffice.operations.routes.letters.RouteToLettersRequest;
import dev.webfx.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.framework.client.operations.route.RouteRequestEmitter;
import dev.webfx.framework.shared.router.auth.authz.RouteRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToLettersRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToLettersRequest(context.getParameter("eventId"), context.getHistory());
    }
}
