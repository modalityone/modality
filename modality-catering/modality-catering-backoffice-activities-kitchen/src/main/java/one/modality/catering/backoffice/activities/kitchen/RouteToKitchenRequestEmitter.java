package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.catering.backoffice.operations.routes.kitchen.RouteToKitchenRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToKitchenRequestEmitter implements RouteRequestEmitter {

  @Override
  public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
    return new RouteToKitchenRequest(context.getHistory());
  }
}
