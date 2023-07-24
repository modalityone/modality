package one.modality.crm.client.activities.login;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.ProvidedLoginUiRoute;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class LoginUiRoute extends UiRouteImpl implements ProvidedLoginUiRoute {

    public LoginUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(
                LoginRouting.getPath(),
                false,
                LoginViewActivity::new,
                ViewDomainActivityContextFinal::new);
    }
}
