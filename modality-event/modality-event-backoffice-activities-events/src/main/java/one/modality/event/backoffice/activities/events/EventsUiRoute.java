package one.modality.event.backoffice.activities.events;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.event.backoffice.activities.events.routing.EventsRouting;

/**
 * @author Bruno Salmon
 */
public final class EventsUiRoute extends UiRouteImpl {

  public EventsUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.createRegex(
        PathBuilder.toRegexPath(EventsRouting.getAnyPath()),
        false,
        EventsActivity::new,
        ViewDomainActivityContextFinal::new);
  }
}
