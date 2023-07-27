package one.modality.base.frontoffice.activities.home;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.frontoffice.activities.home.routing.HomeRouting;

public class HomeUiRoute extends UiRouteImpl {

    public HomeUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(HomeRouting.getPath()
                , false
                , HomeActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
