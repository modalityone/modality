package one.modality.base.backoffice.activities.filters;

import one.modality.base.backoffice.activities.filters.routing.FiltersRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.router.util.PathBuilder;

public final class FiltersUiRoute extends UiRouteImpl {

    public FiltersUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(PathBuilder.toRegexPath(FiltersRouting.getPath())
                , true
                , FiltersActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
