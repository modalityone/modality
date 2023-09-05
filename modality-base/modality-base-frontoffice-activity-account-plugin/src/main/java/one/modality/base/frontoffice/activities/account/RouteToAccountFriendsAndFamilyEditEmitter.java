package one.modality.base.frontoffice.activities.account;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.base.frontoffice.operations.routes.account.RouteToEditAccountFriendsAndFamilyRequest;

public class RouteToAccountFriendsAndFamilyEditEmitter implements RouteRequestEmitter {
    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToEditAccountFriendsAndFamilyRequest(context.getHistory());
    }
}
