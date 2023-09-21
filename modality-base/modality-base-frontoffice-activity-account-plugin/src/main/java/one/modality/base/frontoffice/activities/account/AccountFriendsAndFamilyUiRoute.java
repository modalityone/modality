package one.modality.base.frontoffice.activities.account;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.frontoffice.activities.account.routing.AccountFriendsAndFamilyRouting;

/**
 * @author Bruno Salmon
 */
public final class AccountFriendsAndFamilyUiRoute extends UiRouteImpl {

    public AccountFriendsAndFamilyUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(AccountFriendsAndFamilyRouting.getPath()
                , true
                , AccountFriendsAndFamilyActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
