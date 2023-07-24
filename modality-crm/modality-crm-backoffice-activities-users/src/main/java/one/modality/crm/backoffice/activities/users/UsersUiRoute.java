package one.modality.crm.backoffice.activities.users;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.crm.backoffice.activities.users.routing.UsersRouting;

/**
 * @author Bruno Salmon
 */
public final class UsersUiRoute extends UiRouteImpl {

  public UsersUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.createRegex(
        PathBuilder.toRegexPath(UsersRouting.getPath()),
        true,
        UsersActivity::new,
        ViewDomainActivityContextFinal::new);
  }
}
