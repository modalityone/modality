package one.modality.base.frontoffice.activities.account.personalinfo;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.base.frontoffice.operations.routes.account.RouteToAccountPersonalInformationRequest;

public class RouteToAccountPersonalInformationEmitter implements RouteRequestEmitter {
    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToAccountPersonalInformationRequest(context.getHistory());
    }
}
