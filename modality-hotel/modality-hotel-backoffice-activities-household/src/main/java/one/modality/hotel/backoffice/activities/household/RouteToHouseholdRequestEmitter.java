package one.modality.hotel.backoffice.activities.household;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

import one.modality.hotel.backoffice.operations.routes.household.RouteToHouseholdRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToHouseholdRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToHouseholdRequest(context.getHistory());
    }
}
