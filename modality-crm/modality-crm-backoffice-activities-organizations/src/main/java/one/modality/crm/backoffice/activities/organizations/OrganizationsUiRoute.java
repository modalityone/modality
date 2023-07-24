package one.modality.crm.backoffice.activities.organizations;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.crm.backoffice.activities.organizations.routing.OrganizationsRouting;

/**
 * @author Bruno Salmon
 */
public final class OrganizationsUiRoute extends UiRouteImpl {

  public OrganizationsUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.create(
        OrganizationsRouting.getPath(),
        false,
        OrganizationsActivity::new,
        DomainPresentationActivityContextFinal::new);
  }
}
