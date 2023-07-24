package one.modality.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.ecommerce.backoffice.activities.moneyflows.routing.MoneyFlowsRouting;

/**
 * @author Bruno Salmon
 */
public final class MoneyFlowsUiRoute extends UiRouteImpl {

  public MoneyFlowsUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.createRegex(
        PathBuilder.toRegexPath(MoneyFlowsRouting.getAnyPath()),
        false,
        MoneyFlowsActivity::new,
        ViewDomainActivityContextFinal::new);
  }
}
