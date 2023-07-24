package one.modality.base.backoffice.activities.filters;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.backoffice.activities.filters.routing.FiltersRouting;

public final class FiltersUiRoute extends UiRouteImpl {

  public FiltersUiRoute() {
    super(uiRoute());
  }

  public static UiRoute<?> uiRoute() {
    return UiRoute.createRegex(
        PathBuilder.toRegexPath(FiltersRouting.getPath()),
        false,
        FiltersActivity::new,
        ViewDomainActivityContextFinal::new);
  }
}
