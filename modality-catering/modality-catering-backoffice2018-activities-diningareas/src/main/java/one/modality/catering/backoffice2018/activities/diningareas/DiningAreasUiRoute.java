package one.modality.catering.backoffice2018.activities.diningareas;

import one.modality.catering.backoffice2018.activities.diningareas.routing.DiningAreasRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.router.util.PathBuilder;

/**
 * @author Bruno Salmon
 */
public final class DiningAreasUiRoute extends UiRouteImpl {

    public DiningAreasUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(PathBuilder.toRegexPath(DiningAreasRouting.getPath())
                , false
                , DiningAreasActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
