package one.modality.crm.backoffice.activities.letters;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.crm.backoffice.operations.routes.letters.RouteToLettersRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToLettersRequestEmitter implements RouteRequestEmitter {

  @Override
  public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
    return new RouteToLettersRequest(context.getParameter("eventId"), context.getHistory());
  }
}
