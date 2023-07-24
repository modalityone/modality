package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.hotel.backoffice.activities.accommodation.routing.AccommodationRouting;

/**
 * @author Bruno Salmon
 */
public final class AccommodationUiRoute extends UiRouteImpl {

  public AccommodationUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.createRegex(
        PathBuilder.toRegexPath(AccommodationRouting.getAnyPath()),
        false,
        AccommodationActivity::new,
        ViewDomainActivityContextFinal::new);
  }
}
