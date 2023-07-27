package one.modality.base.frontoffice.activities.account;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.frontoffice.activities.account.routing.AccountRouting;

/**
 * @author Bruno Salmon
 */
public final class AccountUiRoute extends UiRouteImpl {

    public AccountUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(AccountRouting.getPath()
                , true
                , AccountHomeActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
