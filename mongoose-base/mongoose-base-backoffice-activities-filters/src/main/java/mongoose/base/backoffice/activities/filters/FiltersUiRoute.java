package mongoose.base.backoffice.activities.filters;

import mongoose.base.backoffice.activities.filters.routing.FiltersRouting;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;
import dev.webfx.framework.shared.router.util.PathBuilder;

public final class FiltersUiRoute extends UiRouteImpl {

    public FiltersUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(PathBuilder.toRegexPath(FiltersRouting.getPath())
                , false
                , FiltersActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
