package one.modality.ecommerce.backoffice.activities.bookings;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.ecommerce.backoffice.activities.bookings.routing.BookingsRouting;

/**
 * @author Bruno Salmon
 */
public final class BookingsUiRoute extends UiRouteImpl {

  public BookingsUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.createRegex(
        PathBuilder.toRegexPath(BookingsRouting.getAnyPath()),
        false,
        BookingsActivity::new,
        ViewDomainActivityContextFinal::new);
  }
}
