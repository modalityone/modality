package one.modality.base.backoffice.activities.home;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.backoffice.activities.home.routing.HomeRouting;

/**
 * @author Bruno Salmon
 */
public final class HomeUiRoute extends UiRouteImpl {

    public HomeUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(PathBuilder.toRegexPath(HomeRouting.getPath())
                , true
                , HomeActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
