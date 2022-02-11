package mongoose.crm.backoffice.activities.users;

import mongoose.crm.backoffice.activities.users.routing.UsersRouting;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;
import dev.webfx.framework.shared.router.util.PathBuilder;

/**
 * @author Bruno Salmon
 */
public final class UsersUiRoute extends UiRouteImpl {

    public UsersUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(PathBuilder.toRegexPath(UsersRouting.getPath())
                , false
                , UsersActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
