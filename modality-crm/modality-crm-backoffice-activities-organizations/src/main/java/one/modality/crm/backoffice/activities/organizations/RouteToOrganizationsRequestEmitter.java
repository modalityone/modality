package one.modality.crm.backoffice.activities.organizations;

import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.crm.backoffice.operations.routes.organizations.RouteToOrganizationsRequest;

/**
 * @author Bruno Salmon
 */
public final class RouteToOrganizationsRequestEmitter implements RouteRequestEmitter {

  @Override
  public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
    return new RouteToOrganizationsRequest(context.getHistory());
  }
}
