package mongoose.client.activities.unauthorized;

import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.ProvidedUnauthorizedUiRoute;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class UnauthorizedUiRoute extends UiRouteImpl implements ProvidedUnauthorizedUiRoute {

    public UnauthorizedUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(UnauthorizedRouting.getPath()
                , false
                , UnauthorizedViewActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
