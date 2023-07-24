package one.modality.crm.client.activities.unauthorized;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.ProvidedUnauthorizedUiRoute;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class UnauthorizedUiRoute extends UiRouteImpl implements ProvidedUnauthorizedUiRoute {

    public UnauthorizedUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(
                UnauthorizedRouting.getPath(),
                false,
                UnauthorizedViewActivity::new,
                ViewDomainActivityContextFinal::new);
    }
}
