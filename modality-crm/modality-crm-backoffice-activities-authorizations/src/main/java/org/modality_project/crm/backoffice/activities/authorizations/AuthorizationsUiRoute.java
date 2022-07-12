package org.modality_project.crm.backoffice.activities.authorizations;

import org.modality_project.crm.backoffice.activities.authorizations.routing.AuthorizationsRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class AuthorizationsUiRoute extends UiRouteImpl {

    public AuthorizationsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(AuthorizationsRouting.getPath()
                , true
                , AuthorizationsViewActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
