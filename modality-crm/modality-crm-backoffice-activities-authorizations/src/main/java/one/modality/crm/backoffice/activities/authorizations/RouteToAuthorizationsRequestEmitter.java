package one.modality.crm.backoffice.activities.authorizations;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.crm.backoffice.activities.operations.authorizations.RouteToAuthorizationsRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToAuthorizationsRequestEmitter implements RouteRequestEmitter {

  @Override
  public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
    return new RouteToAuthorizationsRequest(context.getHistory());
  }
}
