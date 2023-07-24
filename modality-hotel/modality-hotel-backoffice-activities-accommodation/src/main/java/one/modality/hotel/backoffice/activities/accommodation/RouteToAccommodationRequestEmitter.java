package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.hotel.backoffice.operations.routes.accommodation.RouteToAccommodationRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToAccommodationRequestEmitter implements RouteRequestEmitter {

  @Override
  public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
    return new RouteToAccommodationRequest(context.getHistory());
  }
}
