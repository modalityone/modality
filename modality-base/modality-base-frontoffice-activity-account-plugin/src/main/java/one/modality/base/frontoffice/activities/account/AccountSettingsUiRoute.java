package one.modality.base.frontoffice.activities.account;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.frontoffice.activities.account.routing.AccountSettingsRouting;

/**
 * @author Bruno Salmon
 */
public final class AccountSettingsUiRoute extends UiRouteImpl {

    public AccountSettingsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(AccountSettingsRouting.getPath()
                , true
                , AccountSettingsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
