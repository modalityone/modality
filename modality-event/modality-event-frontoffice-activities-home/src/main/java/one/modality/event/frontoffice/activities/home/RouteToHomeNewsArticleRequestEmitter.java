package one.modality.event.frontoffice.activities.home;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.event.frontoffice.operations.routes.home.RouteToHomeNewsArticleRequest;

public class RouteToHomeNewsArticleRequestEmitter implements RouteRequestEmitter {
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToHomeNewsArticleRequest(context.getHistory());
    }
}
