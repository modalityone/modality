package one.modality.event.frontoffice.activities.home;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.event.frontoffice.activities.home.routing.HomeNewsArticleRouting;
import one.modality.event.frontoffice.activities.home.routing.HomeRouting;

public class HomeNewsArticleUiRoute extends UiRouteImpl {
    public HomeNewsArticleUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(HomeNewsArticleRouting.getPath()
                , false
                , HomeNewsArticleActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
