package one.modality.event.frontoffice.activities.account;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.event.frontoffice.activities.account.routing.AccountFriendsAndFamilyEditRouting;
import one.modality.event.frontoffice.activities.account.routing.AccountFriendsAndFamilyRouting;

/**
 * @author Bruno Salmon
 */
public final class AccountFriendsAndFamilyEditUiRoute extends UiRouteImpl {

    public AccountFriendsAndFamilyEditUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(AccountFriendsAndFamilyEditRouting.getPath()
                , false
                , AccountFriendsAndFamilyEditActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
