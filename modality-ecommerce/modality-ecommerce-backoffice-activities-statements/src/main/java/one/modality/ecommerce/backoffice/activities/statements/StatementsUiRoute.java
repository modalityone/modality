package one.modality.ecommerce.backoffice.activities.statements;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.ecommerce.backoffice.activities.statements.routing.StatementsRouting;

/**
 * @author Bruno Salmon
 */
public final class StatementsUiRoute extends UiRouteImpl {

  public StatementsUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.createRegex(
        PathBuilder.toRegexPath(StatementsRouting.getPath()),
        false,
        StatementsActivity::new,
        ViewDomainActivityContextFinal::new);
  }
}
