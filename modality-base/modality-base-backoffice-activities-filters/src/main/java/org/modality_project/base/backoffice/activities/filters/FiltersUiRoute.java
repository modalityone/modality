package org.modality_project.base.backoffice.activities.filters;

import org.modality_project.base.backoffice.activities.filters.routing.FiltersRouting;
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
                , false
                , FiltersActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
