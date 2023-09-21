package one.modality.base.frontoffice.activities.account;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.base.frontoffice.activities.account.routing.AccountPersonalInformationRouting;

/**
 * @author Bruno Salmon
 */
public final class AccountPersonalInformationUiRoute extends UiRouteImpl {

    public AccountPersonalInformationUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(AccountPersonalInformationRouting.getPath()
                , true
                , AccountPersonalInformationActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
