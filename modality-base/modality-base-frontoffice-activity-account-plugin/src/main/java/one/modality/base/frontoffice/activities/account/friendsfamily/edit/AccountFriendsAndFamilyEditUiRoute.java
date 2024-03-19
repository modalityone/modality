package one.modality.base.frontoffice.activities.account.friendsfamily.edit;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.frontoffice.activities.account.routing.AccountFriendsAndFamilyEditRouting;

/**
 * @author Bruno Salmon
 */
public final class AccountFriendsAndFamilyEditUiRoute extends UiRouteImpl {

    public AccountFriendsAndFamilyEditUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(AccountFriendsAndFamilyEditRouting.getPath()
                , true
                , AccountFriendsAndFamilyEditActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
