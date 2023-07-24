package one.modality.event.frontoffice.activities.program;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.event.frontoffice.activities.program.routing.ProgramRouting;

/**
 * @author Bruno Salmon
 */
public final class ProgramUiRoute extends UiRouteImpl {

  public ProgramUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.create(
        ProgramRouting.getPath(), false, ProgramActivity::new, ViewDomainActivityContextFinal::new);
  }
}
