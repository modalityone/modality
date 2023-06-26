package one.modality.event.frontoffice.activities.account;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.event.frontoffice.operations.routes.account.RouteToAccountFriendsAndFamilyEditRequest;
import one.modality.event.frontoffice.operations.routes.account.RouteToAccountFriendsAndFamilyRequest;

public class RouteToAccountFriendsAndFamilyEditEmitter implements RouteRequestEmitter {
    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToAccountFriendsAndFamilyEditRequest(context.getHistory());
    }
}
