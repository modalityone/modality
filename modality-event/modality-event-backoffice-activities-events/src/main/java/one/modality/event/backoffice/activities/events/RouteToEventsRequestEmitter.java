package one.modality.event.backoffice.activities.events;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.event.backoffice.operations.routes.events.RouteToEventsRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToEventsRequestEmitter implements RouteRequestEmitter {

  @Override
  public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
    return new RouteToEventsRequest(context.getHistory());
  }
}
