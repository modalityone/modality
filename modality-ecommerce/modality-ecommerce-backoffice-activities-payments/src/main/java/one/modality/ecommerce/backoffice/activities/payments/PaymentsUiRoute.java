package one.modality.ecommerce.backoffice.activities.payments;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.ecommerce.backoffice.activities.payments.routing.PaymentsRouting;

/**
 * @author Bruno Salmon
 */
public final class PaymentsUiRoute extends UiRouteImpl {

  public PaymentsUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.createRegex(
        PathBuilder.toRegexPath(PaymentsRouting.getPath()),
        false,
        PaymentsActivity::new,
        ViewDomainActivityContextFinal::new);
  }
}
