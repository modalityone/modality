package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.catering.backoffice.activities.kitchen.routing.KitchenRouting;

/**
 * @author Bruno Salmon
 */
public final class KitchenUiRoute extends UiRouteImpl {

    public KitchenUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(PathBuilder.toRegexPath(KitchenRouting.getPath())
                , true
                , KitchenActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
