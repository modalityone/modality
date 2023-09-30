package one.modality.base.frontoffice.activities.account.friendsfamily;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.base.frontoffice.operations.routes.account.RouteToAccountFriendsAndFamilyRequest;

public class RouteToAccountFriendsAndFamilyEmitter implements RouteRequestEmitter {
    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToAccountFriendsAndFamilyRequest(context.getHistory());
    }
}
