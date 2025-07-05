package one.modality.base.frontoffice.activities.account.friendsfamily;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.base.frontoffice.operations.routes.account.RouteToAccountFriendsAndFamilyRequest;

/**
 * @author Bruno Salmon
 */
public final class AccountFriendsAndFamilyRouting {

    private final static String PATH = "/account/friends-family";

    public static String getPath() {
        return PATH;
    }

    public static final class AccountFriendsAndFamilyUiRoute extends UiRouteImpl {

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

    public static class RouteToAccountFriendsAndFamilyEmitter implements RouteRequestEmitter {
        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToAccountFriendsAndFamilyRequest(context.getHistory());
        }
    }
}
