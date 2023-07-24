package one.modality.ecommerce.frontoffice.activities.payment;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.ecommerce.frontoffice.activities.payment.routing.PaymentRouting;

/**
 * @author Bruno Salmon
 */
public final class PaymentUiRoute extends UiRouteImpl {

  public PaymentUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.create(
        PaymentRouting.getPath(), false, PaymentActivity::new, ViewDomainActivityContextFinal::new);
  }
}
