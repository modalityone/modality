package one.modality.event.backoffice.activities.medias;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.event.backoffice.operations.routes.medias.RouteToMediasRequest;

public final class RouteToMediasRequestEmitter implements RouteRequestEmitter {

    @Override
    public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
        return new RouteToMediasRequest(context.getHistory());
    }
}