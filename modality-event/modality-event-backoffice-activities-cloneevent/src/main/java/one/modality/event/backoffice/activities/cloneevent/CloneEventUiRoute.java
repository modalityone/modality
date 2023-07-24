package one.modality.event.backoffice.activities.cloneevent;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.event.backoffice.activities.cloneevent.routing.CloneEventRouting;

/**
 * @author Bruno Salmon
 */
public final class CloneEventUiRoute extends UiRouteImpl {

  public CloneEventUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.create(
        CloneEventRouting.getPath(),
        false,
        CloneEventActivity::new,
        DomainPresentationActivityContextFinal::new);
  }
}
