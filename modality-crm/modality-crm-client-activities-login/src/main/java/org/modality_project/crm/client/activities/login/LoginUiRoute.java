package org.modality_project.crm.client.activities.login;

import dev.webfx.stack.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.framework.client.ui.uirouter.ProvidedLoginUiRoute;
import dev.webfx.stack.framework.client.ui.uirouter.UiRoute;
import dev.webfx.stack.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class LoginUiRoute extends UiRouteImpl implements ProvidedLoginUiRoute {

    public LoginUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(LoginRouting.getPath()
                , false
                , LoginViewActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
